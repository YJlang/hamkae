package com.example.hamkae.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

/**
 * 이미지 품질 검증 및 전처리를 위한 서비스 클래스
 * 이미지 품질, 중복성, 적절성을 검증합니다.
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-17
 */
@Slf4j
@Service
public class ImageValidationService {

    /**
     * 업로드된 파일을 저장할 기본 디렉토리
     * application.properties에서 설정값을 읽어옵니다.
     */
    @Value("${app.upload.dir}")
    private String uploadDir;

    // 최소 이미지 해상도 (모바일 친화적)
    private static final int MIN_WIDTH = 320;
    private static final int MIN_HEIGHT = 240;
    
    // 최대 이미지 해상도 (모바일 친화적)
    private static final int MAX_WIDTH = 8192;
    private static final int MAX_HEIGHT = 8192;
    
    // 최소 파일 크기 (10KB - 모바일 친화적)
    private static final long MIN_FILE_SIZE = 10 * 1024;
    
    // 최대 파일 크기 제한 없음 (무제한 업로드 허용)
    private static final long MAX_FILE_SIZE = -1;

    /**
     * 이미지 파일의 품질을 검증합니다.
     * 
     * @param file 검증할 이미지 파일
     * @throws IllegalArgumentException 품질 기준을 만족하지 않는 경우
     */
    public void validateImageQuality(MultipartFile file) {
        try {
            // 1. 기본 파일 검증
            validateBasicFile(file);
            
            // 2. 이미지 해상도 검증
            validateImageResolution(file);
            
            // 3. 이미지 내용 검증
            validateImageContent(file);
            
            log.info("이미지 품질 검증 통과: {}", file.getOriginalFilename());
            
        } catch (Exception e) {
            log.error("이미지 품질 검증 실패: {}", file.getOriginalFilename(), e);
            throw new IllegalArgumentException("이미지 품질 검증 실패: " + e.getMessage());
        }
    }

    /**
     * 기본 파일 검증을 수행합니다.
     * 
     * @param file 검증할 파일
     */
    private void validateBasicFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        // 파일 크기 검증
        long fileSize = file.getSize();
        if (fileSize < MIN_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기가 너무 작습니다. 최소 " + (MIN_FILE_SIZE / 1024) + "KB 이상이어야 합니다.");
        }
        // 최대 파일 크기 제한 없음 (무제한 업로드 허용)
        // if (fileSize > MAX_FILE_SIZE) {
        //     throw new IllegalArgumentException("파일 크기가 너무 큽니다. 최대 " + (MAX_FILE_SIZE / 1024 / 1024) + "MB 이하여야 합니다.");
        // }

        // 파일 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }

        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isValidImageExtension(originalFilename)) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다. JPG, JPEG, PNG만 지원합니다.");
        }
    }

    /**
     * 이미지 해상도를 검증합니다.
     * 
     * @param file 검증할 이미지 파일
     * @throws IOException 이미지 읽기 실패 시
     */
    private void validateImageResolution(MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new IllegalArgumentException("이미지를 읽을 수 없습니다. 파일이 손상되었을 수 있습니다.");
        }

        int width = image.getWidth();
        int height = image.getHeight();

        // 최소 해상도 검증
        if (width < MIN_WIDTH || height < MIN_HEIGHT) {
            throw new IllegalArgumentException(
                String.format("이미지 해상도가 너무 낮습니다. 최소 %dx%d 이상이어야 합니다. (현재: %dx%d)", 
                    MIN_WIDTH, MIN_HEIGHT, width, height)
            );
        }

        // 최대 해상도 검증
        if (width > MAX_WIDTH || height > MAX_HEIGHT) {
            throw new IllegalArgumentException(
                String.format("이미지 해상도가 너무 높습니다. 최대 %dx%d 이하여야 합니다. (현재: %dx%d)", 
                    MAX_WIDTH, MAX_HEIGHT, width, height)
            );
        }

        log.debug("이미지 해상도 검증 통과: {}x{}", width, height);
    }

    /**
     * 이미지 내용을 검증합니다.
     * 
     * @param file 검증할 이미지 파일
     * @throws IOException 이미지 읽기 실패 시
     */
    private void validateImageContent(MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        
        // 이미지가 너무 어둡거나 밝지 않은지 검증
        double averageBrightness = calculateAverageBrightness(image);
        if (averageBrightness < 30) {
            throw new IllegalArgumentException("이미지가 너무 어둡습니다. 더 밝은 환경에서 촬영해주세요.");
        }
        if (averageBrightness > 220) {
            throw new IllegalArgumentException("이미지가 너무 밝습니다. 더 적절한 밝기로 촬영해주세요.");
        }

        log.debug("이미지 밝기 검증 통과: 평균 밝기 {}", averageBrightness);
    }

    /**
     * 이미지의 평균 밝기를 계산합니다.
     * 
     * @param image 분석할 이미지
     * @return 평균 밝기 (0-255)
     */
    private double calculateAverageBrightness(BufferedImage image) {
        long totalBrightness = 0;
        int pixelCount = 0;

        for (int y = 0; y < image.getHeight(); y += 10) { // 10픽셀마다 샘플링
            for (int x = 0; x < image.getWidth(); x += 10) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                
                // RGB를 그레이스케일로 변환
                int brightness = (r + g + b) / 3;
                totalBrightness += brightness;
                pixelCount++;
            }
        }

        return pixelCount > 0 ? (double) totalBrightness / pixelCount : 0;
    }

    /**
     * 이미지 파일 확장자가 유효한지 검증합니다.
     * 
     * @param filename 파일명
     * @return 유효한 확장자이면 true
     */
    private boolean isValidImageExtension(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png");
    }

    /**
     * 파일명에서 확장자를 추출합니다.
     * 
     * @param filename 파일명
     * @return 파일 확장자
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }

    /**
     * 이미지를 Base64로 인코딩합니다.
     * GPT Vision API에 전달하기 위해 사용됩니다.
     * 
     * @param imagePath 이미지 파일 경로
     * @return Base64로 인코딩된 이미지 문자열
     * @throws IOException 파일 읽기 실패 시
     */
    public String encodeImageToBase64(String imagePath) throws IOException {
        try {
            // 파일 경로에서 실제 파일 읽기
            // imagePath는 "/images/2025/08/17/filename.jpg" 형태
            // 실제 파일은 환경 변수로 설정된 경로에 저장됨
            String relativePath = imagePath.replace("/images/", "");
            Path fullPath = Paths.get(uploadDir, relativePath);
            File file = fullPath.toFile();
            
            if (!file.exists()) {
                log.warn("이미지 파일을 찾을 수 없습니다: {}", fullPath);
                throw new IOException("이미지 파일을 찾을 수 없습니다: " + imagePath);
            }

            // 이미지 압축 및 최적화
            BufferedImage originalImage = ImageIO.read(file);
            if (originalImage == null) {
                throw new IOException("이미지를 읽을 수 없습니다: " + imagePath);
            }
            
            // 색상 공간을 RGB로 강제 변환
            BufferedImage rgbImage = convertToRGB(originalImage);
            
            // 이미지 크기 조정 (최대 800x600으로 제한)
            BufferedImage resizedImage = resizeImage(rgbImage, 800, 600);
            
            // 압축된 이미지를 Base64로 인코딩
            String base64Image = encodeBufferedImageToBase64(resizedImage, "jpg", 0.7f);
            
            log.debug("이미지 Base64 인코딩 완료: {} -> {} bytes (압축됨)", imagePath, base64Image.length());
            return base64Image;
            
        } catch (Exception e) {
            log.error("이미지 Base64 인코딩 실패: {}", imagePath, e);
            throw new IOException("이미지 인코딩 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 이미지 색상 공간을 RGB로 변환합니다.
     * 
     * @param originalImage 원본 이미지
     * @return RGB 모드로 변환된 이미지
     */
    private BufferedImage convertToRGB(BufferedImage originalImage) {
        // 이미 RGB 모드인 경우 그대로 반환
        if (originalImage.getType() == BufferedImage.TYPE_INT_RGB) {
            return originalImage;
        }
        
        // RGB 모드로 새 이미지 생성
        BufferedImage rgbImage = new BufferedImage(
            originalImage.getWidth(), 
            originalImage.getHeight(), 
            BufferedImage.TYPE_INT_RGB
        );
        
        // 원본 이미지를 RGB 이미지에 그리기
        java.awt.Graphics2D g2d = rgbImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, null);
        g2d.dispose();
        
        log.debug("이미지 색상 공간을 RGB로 변환 완료: {} -> RGB", originalImage.getType());
        return rgbImage;
    }

    /**
     * 이미지 크기를 조정합니다.
     * 
     * @param originalImage 원본 이미지
     * @param maxWidth 최대 너비
     * @param maxHeight 최대 높이
     * @return 크기가 조정된 이미지
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int maxWidth, int maxHeight) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // 비율 유지하면서 크기 조정
        double scale = Math.min((double) maxWidth / originalWidth, (double) maxHeight / originalHeight);
        
        if (scale >= 1.0) {
            // 원본이 더 작으면 그대로 반환
            return originalImage;
        }
        
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);
        
        // 새로운 이미지 생성
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        
        return resizedImage;
    }

    /**
     * BufferedImage를 Base64로 인코딩합니다.
     * 
     * @param image 인코딩할 이미지
     * @param format 이미지 형식 (jpg, png 등)
     * @param quality 품질 (0.0 ~ 1.0)
     * @return Base64로 인코딩된 이미지 문자열
     * @throws IOException 인코딩 실패 시
     */
    private String encodeBufferedImageToBase64(BufferedImage image, String format, float quality) throws IOException {
        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
            // JPEG 품질 설정
            if ("jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format)) {
                javax.imageio.ImageWriteParam param = ImageIO.getImageWritersByFormatName("jpeg").next().getDefaultWriteParam();
                if (param.canWriteCompressed()) {
                    param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(quality);
                }
                
                javax.imageio.ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
                javax.imageio.stream.ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
                writer.setOutput(ios);
                writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
                writer.dispose();
                ios.close();
            } else {
                // PNG 등 다른 형식
                ImageIO.write(image, format, baos);
            }
            
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        }
    }

    /**
     * 이미지가 중복 업로드인지 검증합니다.
     * 
     * @param markerId 마커 ID
     * @param photoType 사진 타입
     * @param currentUserId 현재 사용자 ID
     * @param existingPhotos 기존 사진 목록
     * @throws IllegalArgumentException 중복 업로드인 경우
     */
    public void validateDuplicateUpload(Long markerId, String photoType, Long currentUserId, int existingPhotos) {
        // 같은 마커에 같은 타입의 사진이 이미 있는지 확인
        if (existingPhotos > 0) {
            log.warn("중복 업로드 시도: markerId={}, type={}, userId={}", markerId, photoType, currentUserId);
            throw new IllegalArgumentException(
                String.format("이미 %s 사진이 업로드되어 있습니다. 중복 업로드는 불가능합니다.", photoType)
            );
        }
    }
}
