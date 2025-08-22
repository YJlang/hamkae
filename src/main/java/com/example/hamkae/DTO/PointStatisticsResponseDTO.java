package com.example.hamkae.DTO;

import com.example.hamkae.service.PointHistoryService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 포인트 통계 조회를 위한 응답 DTO
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointStatisticsResponseDTO {

    /**
     * 총 적립 포인트
     */
    private Integer totalEarned;

    /**
     * 총 사용 포인트
     */
    private Integer totalUsed;

    /**
     * 현재 보유 포인트
     */
    private Integer currentPoints;

    /**
     * 사용 가능 포인트
     */
    private Integer availablePoints;

    /**
     * PointStatistics를 PointStatisticsResponseDTO로 변환하는 정적 팩토리 메서드
     * 
     * @param statistics 변환할 PointStatistics 객체
     * @return PointStatisticsResponseDTO 객체
     */
    public static PointStatisticsResponseDTO from(PointHistoryService.PointStatistics statistics) {
        return PointStatisticsResponseDTO.builder()
                .totalEarned(statistics.getTotalEarned())
                .totalUsed(statistics.getTotalUsed())
                .currentPoints(statistics.getCurrentPoints())
                .availablePoints(statistics.getAvailablePoints())
                .build();
    }
}
