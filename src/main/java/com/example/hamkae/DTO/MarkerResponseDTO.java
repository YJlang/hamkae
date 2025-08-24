package com.example.hamkae.DTO;

import com.example.hamkae.domain.Marker;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 마커 조회를 위한 응답 DTO
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkerResponseDTO {

    /**
     * 마커 고유 식별자
     */
    private Long id;

    /**
     * 마커의 위도 좌표
     */
    private BigDecimal lat;

    /**
     * 마커의 경도 좌표
     */
    private BigDecimal lng;

    /**
     * 쓰레기 위치에 대한 설명
     */
    private String description;

    /**
     * 쓰레기 위치의 실제 주소
     */
    private String address;

    /**
     * 마커를 제보한 사용자 정보
     */
    private ReporterInfoDTO reporter;

    /**
     * 마커의 현재 상태
     */
    private String status;

    /**
     * 마커 등록일시
     */
    private LocalDateTime createdAt;

    /**
     * 마커 정보 수정일시
     */
    private LocalDateTime updatedAt;

    /**
     * 마커에 연결된 사진들 (간단한 정보만)
     */
    private List<PhotoSimpleDTO> photos;

    /**
     * Marker 엔티티를 MarkerResponseDTO로 변환하는 정적 팩토리 메서드
     * 
     * @param marker 변환할 Marker 엔티티
     * @return MarkerResponseDTO 객체
     */
    public static MarkerResponseDTO from(Marker marker) {
        // 디버깅: 주소 정보 확인
        String markerAddress = marker.getAddress();
        System.out.println("[DEBUG] MarkerResponseDTO.from - 마커 ID: " + marker.getId() + ", 주소: " + markerAddress);
        
        return MarkerResponseDTO.builder()
                .id(marker.getId())
                .lat(marker.getLat())
                .lng(marker.getLng())
                .description(marker.getDescription())
                .address(marker.getAddress())
                .status(marker.getStatus().name())
                .createdAt(marker.getCreatedAt())
                .updatedAt(marker.getUpdatedAt())
                .reporter(ReporterInfoDTO.from(marker.getReportedBy()))
                .photos(marker.getPhotos().stream()
                        .map(PhotoSimpleDTO::from)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * 사진 간단 정보를 담는 내부 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class PhotoSimpleDTO {
        private Long id;
        private String type;
        private String imagePath;
        private String gptResponse; // GPT API 응답 결과 추가
        private LocalDateTime verifiedAt; // AI 검증 완료 시점 추가

        public static PhotoSimpleDTO from(com.example.hamkae.domain.Photo photo) {
            // 디버깅: GPT 응답과 검증 시점 확인
            System.out.println("[DEBUG] PhotoSimpleDTO.from - 사진 ID: " + photo.getId() + 
                             ", 타입: " + photo.getType() + 
                             ", GPT 응답: " + (photo.getGptResponse() != null ? "있음" : "없음") + 
                             ", 검증시점: " + photo.getVerifiedAt());
            
            return PhotoSimpleDTO.builder()
                    .id(photo.getId())
                    .type(photo.getType().name())
                    .imagePath(photo.getImagePath())
                    .gptResponse(photo.getGptResponse()) // GPT 응답 포함
                    .verifiedAt(photo.getVerifiedAt()) // AI 검증 완료 시점 포함
                    .build();
        }

        // Lombok @Data가 제대로 작동하지 않을 경우를 대비한 수동 getter 메서드
        public String getGptResponse() {
            return this.gptResponse;
        }

        public LocalDateTime getVerifiedAt() {
            return this.verifiedAt;
        }
    }

    /**
     * 제보자 정보를 담는 내부 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReporterInfoDTO {
        private Long id;
        private String username;
        private String name;

        public static ReporterInfoDTO from(com.example.hamkae.domain.User user) {
            if (user == null) return null;
            return ReporterInfoDTO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .name(user.getName())
                    .build();
        }
    }
}
