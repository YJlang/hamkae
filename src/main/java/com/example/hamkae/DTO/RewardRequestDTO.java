package com.example.hamkae.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 상품권 교환 요청을 위한 DTO
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardRequestDTO {

    /**
     * 교환에 사용할 포인트 수량
     */
    private Integer pointsUsed;

    /**
     * 교환할 상품권 타입
     * 예: "온누리상품권", "시장상품권" 등
     */
    private String rewardType;
}
