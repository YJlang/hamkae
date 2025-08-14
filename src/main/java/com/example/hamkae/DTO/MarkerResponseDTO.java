package com.example.hamkae.DTO;

import com.example.hamkae.domain.Marker;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     * 마커의 현재 상태
     */
    private String status;

    /**
     * 마커 등록일시
     */
    private LocalDateTime createdAt;

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
        return MarkerResponseDTO.builder()
                .id(marker.getId())
                .lat(marker.getLat())
                .lng(marker.getLng())
                .description(marker.getDescription())
                .status(marker.getStatus().name())
                .createdAt(marker.getCreatedAt())
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
    public static class PhotoSimpleDTO {
        private Long id;
        private String type;
        private String imagePath;

        public static PhotoSimpleDTO from(com.example.hamkae.domain.Photo photo) {
            return PhotoSimpleDTO.builder()
                    .id(photo.getId())
                    .type(photo.getType().name())
                    .imagePath(photo.getImagePath())
                    .build();
        }
    }
}
