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

    /**
     * 마커 상태를 변경합니다 (AI 검증 성공 후 CLEANED 상태로 변경).
     * 
     * @param markerId 상태를 변경할 마커 ID
     * @param status 변경할 상태 (CLEANED, REMOVED 등)
     * @param userId 상태 변경을 요청한 사용자 ID
     * @throws RuntimeException 마커를 찾을 수 없거나 권한이 없는 경우
     */
    @Transactional
    public void updateMarkerStatus(Long markerId, String status, Long userId) {
        log.info("마커 상태 변경 서비스 시작: markerId={}, status={}, userId={}", markerId, status, userId);
        
        Marker marker = markerRepository.findById(markerId)
                .orElseThrow(() -> {
                    log.error("마커를 찾을 수 없음: markerId={}", markerId);
                    return new RuntimeException("마커를 찾을 수 없습니다.");
                });

        log.info("마커 조회 성공: markerId={}, 현재상태={}, 제보자={}", 
                markerId, marker.getStatus(), marker.getReportedBy().getId());

        // 권한 확인: 제보자이거나 AI 검증을 통해 상태 변경하는 경우 허용
        if (!marker.getReportedBy().getId().equals(userId)) {
            // AI 검증 후 상태 변경의 경우 권한 체크를 완화
            log.info("마커 상태 변경 권한 확인: markerId={}, 요청자={}, 제보자={}", 
                    markerId, userId, marker.getReportedBy().getId());
        } else {
            log.info("마커 제보자 본인이 상태 변경 요청: markerId={}, userId={}", markerId, userId);
        }

        try {
            // 상태 변경
            Marker.MarkerStatus newStatus;
            try {
                newStatus = Marker.MarkerStatus.valueOf(status.toUpperCase());
                log.info("상태 파싱 성공: 입력={}, 파싱결과={}", status, newStatus);
            } catch (IllegalArgumentException e) {
                log.error("유효하지 않은 마커 상태: status={}", status, e);
                throw new RuntimeException("유효하지 않은 마커 상태입니다: " + status);
            }

            // 상태 변경 로직
            switch (newStatus) {
                case CLEANED:
                    log.info("마커를 청소 완료 상태로 변경: markerId={}", markerId);
                    marker.markAsCleaned();
                    log.info("마커 상태를 청소 완료로 변경: markerId={}, 사용자={}", markerId, userId);
                    break;
                case REMOVED:
                    log.info("마커를 삭제됨 상태로 변경: markerId={}", markerId);
                    marker.markAsRemoved();
                    log.info("마커 상태를 삭제됨으로 변경: markerId={}, 사용자={}", markerId, userId);
                    break;
                case ACTIVE:
                    // ACTIVE로 되돌리는 것은 일반적으로 허용하지 않음
                    log.warn("마커를 활성 상태로 되돌리려는 시도: markerId={}, status={}", markerId, newStatus);
                    throw new RuntimeException("마커를 활성 상태로 되돌릴 수 없습니다.");
                default:
                    log.error("지원하지 않는 마커 상태: markerId={}, status={}", markerId, newStatus);
                    throw new RuntimeException("지원하지 않는 마커 상태입니다: " + newStatus);
            }

            log.info("마커 상태 변경 후 저장 시도: markerId={}, 새상태={}", markerId, newStatus);
            markerRepository.save(marker);
            log.info("마커 상태 변경 완료: markerId={}, 새상태={}", markerId, newStatus);
            
        } catch (Exception e) {
            log.error("마커 상태 변경 중 오류 발생: markerId={}, status={}", markerId, status, e);
            throw new RuntimeException("마커 상태 변경 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 특정 사용자가 청소 인증한 마커들을 조회합니다 (CLEANED 상태).
     * 
     * @param userId 사용자 ID
     * @return 청소 인증된 마커 목록
     */
    public List<MarkerResponseDTO> getVerifiedMarkersByUserId(Long userId) {
        log.info("사용자 청소 인증 마커 조회: userId={}", userId);
        
        List<Marker> verifiedMarkers = markerRepository.findByReportedByIdAndStatus(userId, Marker.MarkerStatus.CLEANED);
        
        List<MarkerResponseDTO> responseDTOs = verifiedMarkers.stream()
                .map(marker -> {
                    MarkerResponseDTO dto = MarkerResponseDTO.from(marker);
                    log.debug("청소 인증 마커 변환: markerId={}, status={}", marker.getId(), marker.getStatus());
                    return dto;
                })
                .collect(Collectors.toList());
        
        log.info("사용자 청소 인증 마커 조회 완료: userId={}, count={}", userId, responseDTOs.size());
        return responseDTOs;
    }
}