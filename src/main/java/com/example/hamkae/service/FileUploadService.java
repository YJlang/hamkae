package com.example.hamkae.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 파일 업로드를 처리하는 서비스 클래스
 * 이미지 파일을 로컬 저장소에 저장하고 접근 경로를 반환합니다.
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-13
 */
@Service
@Slf4j
public class FileUploadService {

    /**
     * 업로드된 파일을 저장할 기본 디렉토리
     * application.properties에서 설정값을 읽어옵니다.
     */
    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * 이미지 파일을 업로드하고 저장 경로를 반환합니다.
     * 
     * @param file 업로드할 이미지 파일
     * @return 저장된 파일의 접근 경로 (예: /images/2025/08/13/filename.jpg)
     * @throws IOException 파일 저장 중 오류 발생 시
     */
    public String uploadImage(MultipartFile file) throws IOException {
        // 파일 유효성 검사
        validateImageFile(file);

        // 업로드 디렉토리 생성
        String uploadPath = createUploadDirectory();
        
        // 고유한 파일명 생성
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = generateUniqueFilename(fileExtension);
        
        // 파일 저장
        Path filePath = Paths.get(uploadPath, uniqueFilename);
        Files.copy(file.getInputStream(), filePath);
        
        // 접근 경로 반환 (웹에서 접근 가능한 경로)
        String accessPath = "/images/" + getDatePathForWeb() + "/" + uniqueFilename;
        
        log.info("이미지 업로드 완료: {} -> {}", originalFilename, accessPath);
        log.info("저장 경로: {}", filePath.toAbsolutePath());
        return accessPath;
    }

    /**
     * 이미지 파일의 유효성을 검사합니다.
     * 
     * @param file 검사할 파일
     * @throws IllegalArgumentException 유효하지 않은 파일인 경우
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }

        // 파일 크기 제한 (10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }
    }

    /**
     * 업로드 디렉토리를 생성합니다.
     * 기본 uploads/images 폴더와 날짜별 하위 폴더를 모두 생성합니다.
     * 
     * @return 생성된 디렉토리 경로
     * @throws IOException 디렉토리 생성 실패 시
     */
    private String createUploadDirectory() throws IOException {
        // 1. 기본 uploads/images 폴더 생성
        Path baseDir = Paths.get(uploadDir);
        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
            log.info("기본 업로드 디렉토리 생성: {}", baseDir.toAbsolutePath());
        }
        
        // 2. 날짜별 하위 폴더 생성
        String datePath = getDatePath();
        String fullPath = uploadDir + datePath;
        
        Path directory = Paths.get(fullPath);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
            log.info("날짜별 업로드 디렉토리 생성: {}", directory.toAbsolutePath());
        }
        
        return fullPath;
    }

    /**
     * 현재 날짜를 기반으로 한 경로를 생성합니다 (운영체제 호환).
     * Windows: 2025\08\13, Unix: 2025/08/13
     * 
     * @return 날짜 기반 경로
     */
    private String getDatePath() {
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();
        
        // 운영체제 호환 경로 생성
        return year + File.separator + 
               String.format("%02d", month) + File.separator + 
               String.format("%02d", day);
    }

    /**
     * 웹 접근용 날짜 경로를 생성합니다 (항상 / 사용).
     * 예: 2025/08/13
     * 
     * @return 웹 접근용 날짜 경로
     */
    private String getDatePathForWeb() {
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();
        
        // 웹 접근용 경로는 항상 / 사용
        return year + "/" + 
               String.format("%02d", month) + "/" + 
               String.format("%02d", day);
    }

    /**
     * 파일 확장자를 추출합니다.
     * 
     * @param filename 원본 파일명
     * @return 파일 확장자 (점 포함)
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return ".jpg"; // 기본 확장자
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * 고유한 파일명을 생성합니다.
     * 
     * @param extension 파일 확장자
     * @return 고유한 파일명
     */
    private String generateUniqueFilename(String extension) {
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * 업로드된 파일을 삭제합니다.
     * 
     * @param filePath 삭제할 파일 경로
     * @return 삭제 성공 여부
     */
    public boolean deleteImage(String filePath) {
        try {
            // /images/ 경로를 제거하고 실제 파일 경로로 변환
            String relativePath = filePath.replace("/images/", "");
            Path fullPath = Paths.get(uploadDir, relativePath);
            
            if (Files.exists(fullPath)) {
                Files.delete(fullPath);
                log.info("이미지 삭제 완료: {}", filePath);
                return true;
            }
            return false;
        } catch (IOException e) {
            log.error("이미지 삭제 실패: {}", filePath, e);
            return false;
        }
    }
}
