package com.example.hamkae.service;

import com.example.hamkae.domain.Marker;
import com.example.hamkae.domain.Photo;
import com.example.hamkae.domain.User;
import com.example.hamkae.repository.MarkerRepository;
import com.example.hamkae.repository.PhotoRepository;
import com.example.hamkae.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 사진 관련 비즈니스 로직을 처리하는 서비스 클래스
 * 청소 인증용 사진 업로드 및 사진 조회 기능을 제공합니다.
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-13
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final MarkerRepository markerRepository;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;

    /**
     * 청소 인증용 사진들을 업로드합니다.
     * 
     * @param markerId 마커 ID
     * @param images 업로드할 이미지 파일들
     * @param userId 업로드하는 사용자 ID
     * @return 업로드된 사진들의 ID 리스트
     * @throws RuntimeException 마커나 사용자를 찾을 수 없는 경우
     */
    @Transactional
    public List<Long> uploadCleanupPhotos(Long markerId, MultipartFile[] images, Long userId) {
        // 마커 존재 여부 확인
        Marker marker = markerRepository.findById(markerId)
                .orElseThrow(() -> new RuntimeException("마커를 찾을 수 없습니다."));
        
        // 사용자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 청소 인증용 사진은 무조건 AFTER 타입
        Photo.PhotoType photoType = Photo.PhotoType.AFTER;
        
        List<Long> photoIds = new ArrayList<>();
        
        try {
            if (images != null && images.length > 0) {
                for (MultipartFile image : images) {
                    if (!image.isEmpty()) {
                        // 이미지 파일 업로드
                        String imagePath = fileUploadService.uploadImage(image);
                        
                        // Photo 엔티티 생성
                        Photo photo = Photo.builder()
                                .marker(marker)
                                .user(user)
                                .imagePath(imagePath)
                                .type(photoType)
                                .build();
                        
                        // 사진 저장
                        Photo savedPhoto = photoRepository.save(photo);
                        photoIds.add(savedPhoto.getId());
                        
                        // 마커에 사진 추가 (양방향 관계 설정)
                        marker.addPhoto(savedPhoto);
                        
                        log.info("청소 인증용 사진 업로드 완료: markerId={}, type=AFTER, photoId={}", 
                                markerId, savedPhoto.getId());
                    }
                }
                
                // 마커 저장 (양방향 관계 설정)
                markerRepository.save(marker);
            }
            
            log.info("청소 인증용 사진들 업로드 완료: markerId={}, type=AFTER, count={}", 
                    markerId, photoIds.size());
            
            return photoIds;
            
        } catch (IOException e) {
            log.error("이미지 파일 업로드 실패: markerId={}, photoType=AFTER", markerId, e);
            throw new RuntimeException("이미지 파일 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 특정 사진의 상세 정보를 조회합니다.
     * 
     * @param photoId 조회할 사진 ID
     * @return 사진 상세 정보
     * @throws RuntimeException 사진을 찾을 수 없는 경우
     */
    public Map<String, Object> getPhotoById(Long photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("사진을 찾을 수 없습니다."));
        
        Map<String, Object> photoInfo = new HashMap<>();
        photoInfo.put("id", photo.getId());
        photoInfo.put("marker_id", photo.getMarker().getId());
        photoInfo.put("user_id", photo.getUser().getId());
        photoInfo.put("image_path", photo.getImagePath());
        photoInfo.put("type", photo.getType().name());
        photoInfo.put("verification_status", photo.getVerificationStatus().name());
        photoInfo.put("created_at", photo.getCreatedAt());
        
        if (photo.getGptResponse() != null) {
            photoInfo.put("gpt_response", photo.getGptResponse());
        }
        
        return photoInfo;
    }

    /**
     * 특정 마커의 모든 사진을 조회합니다.
     * 
     * @param markerId 마커 ID
     * @return 해당 마커의 사진 목록
     * @throws RuntimeException 마커를 찾을 수 없는 경우
     */
    public Map<String, Object> getPhotosByMarkerId(Long markerId) {
        Marker marker = markerRepository.findById(markerId)
                .orElseThrow(() -> new RuntimeException("마커를 찾을 수 없습니다."));
        
        List<Photo> photos = photoRepository.findByMarkerId(markerId);
        
        Map<String, Object> photosInfo = new HashMap<>();
        photosInfo.put("marker_id", markerId);
        photosInfo.put("total_count", photos.size());
        
        List<Map<String, Object>> photoList = photos.stream()
                .map(photo -> {
                    Map<String, Object> photoMap = new HashMap<>();
                    photoMap.put("id", photo.getId());
                    photoMap.put("type", photo.getType().name());
                    photoMap.put("image_path", photo.getImagePath());
                    photoMap.put("verification_status", photo.getVerificationStatus().name());
                    photoMap.put("created_at", photo.getCreatedAt());
                    return photoMap;
                })
                .collect(Collectors.toList());
        
        photosInfo.put("photos", photoList);
        
        return photosInfo;
    }
}
