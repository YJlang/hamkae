package com.example.hamkae.DTO;

import com.example.hamkae.domain.PointHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 포인트 이력 조회를 위한 응답 DTO
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointHistoryResponseDTO {

    /**
     * 포인트 이력 고유 식별자
     */
    private Long id;

    /**
     * 변동된 포인트 수량 (양수: 적립, 음수: 사용)
     */
    private Integer points;

    /**
     * 포인트 변동 타입
     */
    private String type;

    /**
     * 포인트 변동 사유
     */
    private String description;

    /**
     * 관련 사진 ID (적립 시에만)
     */
    private Long relatedPhotoId;

    /**
     * 포인트 변동일시
     */
    private LocalDateTime createdAt;

    /**
     * PointHistory 엔티티를 PointHistoryResponseDTO로 변환하는 정적 팩토리 메서드
     * 
     * @param pointHistory 변환할 PointHistory 엔티티
     * @return PointHistoryResponseDTO 객체
     */
    public static PointHistoryResponseDTO from(PointHistory pointHistory) {
        return PointHistoryResponseDTO.builder()
                .id(pointHistory.getId())
                .points(pointHistory.getPoints())
                .type(pointHistory.getType().name())
                .description(pointHistory.getDescription())
                .relatedPhotoId(pointHistory.getRelatedPhoto() != null ? 
                        pointHistory.getRelatedPhoto().getId() : null)
                .createdAt(pointHistory.getCreatedAt())
                .build();
    }
}
