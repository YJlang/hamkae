package com.example.hamkae.service;

import com.example.hamkae.domain.Marker;
import com.example.hamkae.domain.Photo;
import com.example.hamkae.domain.User;
import com.example.hamkae.DTO.MarkerRequestDTO;
import com.example.hamkae.DTO.MarkerResponseDTO;
import com.example.hamkae.repository.MarkerRepository;
import com.example.hamkae.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * 마커 관련 비즈니스 로직을 처리하는 서비스 클래스
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-13
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MarkerService {

    private final MarkerRepository markerRepository;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;

    /**
     * 새로운 마커를 등록합니다.
     * 
     * @param request 마커 등록 요청 데이터
     * @param userId 마커를 등록할 사용자 ID
     * @return 등록된 마커의 ID
     * @throws RuntimeException 사용자를 찾을 수 없는 경우
     */
    @Transactional
    public Long registerMarker(MarkerRequestDTO request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Marker marker = Marker.builder()
                .lat(request.getLat())
                .lng(request.getLng())
                .description(request.getDescription())
                .reportedBy(user)
                .build();

        Marker savedMarker = markerRepository.save(marker);
        return savedMarker.getId();
    }

    /**
     * 마커에 사진을 추가합니다.
     * 
     * @param markerId 마커 ID
     * @param imagePath 이미지 파일 경로
     * @param photoType 사진 타입 (REPORT, BEFORE, AFTER)
     * @param userId 사진을 업로드한 사용자 ID
     * @return 추가된 사진의 ID
     * @throws RuntimeException 마커나 사용자를 찾을 수 없는 경우
     */
    @Transactional
    public Long addPhotoToMarker(Long markerId, String imagePath, String photoType, Long userId) {
        Marker marker = markerRepository.findById(markerId)
                .orElseThrow(() -> new RuntimeException("마커를 찾을 수 없습니다."));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Photo.PhotoType type;
        try {
            type = Photo.PhotoType.valueOf(photoType);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("유효하지 않은 사진 타입입니다: " + photoType);
        }

        Photo photo = Photo.builder()
                .marker(marker)
                .user(user)
                .imagePath(imagePath)
                .type(type)
                .build();

        // 마커에 사진 추가 (양방향 관계 설정)
        marker.addPhoto(photo);
        markerRepository.save(marker);

        return photo.getId();
    }

    /**
     * 모든 활성 마커를 조회합니다.
     * 
     * @return 활성 상태의 마커 목록
     */
    public List<MarkerResponseDTO> getAllActiveMarkers() {
        List<Marker> markers = markerRepository.findActiveMarkers();
        return markers.stream()
                .map(MarkerResponseDTO::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 마커의 상세 정보를 조회합니다.
     * 
     * @param markerId 조회할 마커 ID
     * @return 마커 상세 정보
     * @throws RuntimeException 마커를 찾을 수 없는 경우
     */
    public MarkerResponseDTO getMarkerById(Long markerId) {
        Marker marker = markerRepository.findById(markerId)
                .orElseThrow(() -> new RuntimeException("마커를 찾을 수 없습니다."));
        return MarkerResponseDTO.from(marker);
    }

    /**
     * 특정 사용자가 제보한 마커들을 조회합니다.
     * 
     * @param userId 제보자 사용자 ID
     * @return 해당 사용자가 제보한 마커 목록
     */
    public List<MarkerResponseDTO> getMarkersByUserId(Long userId) {
        List<Marker> markers = markerRepository.findByReportedById(userId);
        return markers.stream()
                .map(MarkerResponseDTO::from)
                .collect(Collectors.toList());
    }

    /**
     * 마커를 완전히 삭제합니다 (제보자만 가능).
     * 마커와 연결된 모든 사진의 로컬 파일도 함께 삭제됩니다.
     * 
     * @param markerId 삭제할 마커 ID
     * @param userId 삭제를 요청한 사용자 ID
     * @throws RuntimeException 마커를 찾을 수 없거나 권한이 없는 경우
     */
    @Transactional
    public void deleteMarker(Long markerId, Long userId) {
        Marker marker = markerRepository.findById(markerId)
                .orElseThrow(() -> new RuntimeException("마커를 찾을 수 없습니다."));

        if (!marker.getReportedBy().getId().equals(userId)) {
            throw new RuntimeException("마커를 삭제할 권한이 없습니다.");
        }

        try {
            // 연결된 사진들의 로컬 파일 삭제
            List<String> deletedFiles = new ArrayList<>();
            for (Photo photo : marker.getPhotos()) {
                String imagePath = photo.getImagePath();
                if (fileUploadService.deleteImage(imagePath)) {
                    deletedFiles.add(imagePath);
                    log.info("사진 파일 삭제 완료: {}", imagePath);
                } else {
                    log.warn("사진 파일 삭제 실패: {}", imagePath);
                }
            }

            // 마커와 연결된 모든 사진 삭제 (CASCADE로 자동 삭제됨)
            markerRepository.delete(marker);
            
            log.info("마커 완전 삭제 완료: ID={}, 삭제된 사진 파일 수={}", markerId, deletedFiles.size());
            
        } catch (Exception e) {
            log.error("마커 삭제 중 오류 발생: markerId={}", markerId, e);
            throw new RuntimeException("마커 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}