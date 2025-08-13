package com.example.hamkae.DTO;

import com.example.hamkae.domain.Reward;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상품권 교환 조회를 위한 응답 DTO
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardResponseDTO {

    /**
     * 상품권 교환 요청 고유 식별자
     */
    private Long id;

    /**
     * 교환에 사용된 포인트 수량
     */
    private Integer pointsUsed;

    /**
     * 교환할 상품권 타입
     */
    private String rewardType;

    /**
     * 교환 요청의 처리 상태
     */
    private String status;

    /**
     * 교환 요청일시
     */
    private LocalDateTime createdAt;

    /**
     * 교환 처리 완료일시
     */
    private LocalDateTime processedAt;

    /**
     * Reward 엔티티를 RewardResponseDTO로 변환하는 정적 팩토리 메서드
     * 
     * @param reward 변환할 Reward 엔티티
     * @return RewardResponseDTO 객체
     */
    public static RewardResponseDTO from(Reward reward) {
        return RewardResponseDTO.builder()
                .id(reward.getId())
                .pointsUsed(reward.getPointsUsed())
                .rewardType(reward.getRewardType())
                .status(reward.getStatus().name())
                .createdAt(reward.getCreatedAt())
                .processedAt(reward.getProcessedAt())
                .build();
    }
}
