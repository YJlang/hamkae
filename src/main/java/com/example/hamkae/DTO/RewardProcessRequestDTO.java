package com.example.hamkae.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 상품권 교환 승인/거부 요청을 위한 DTO
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardProcessRequestDTO {

    /**
     * 처리할 상품권 교환 요청 ID
     */
    private Long rewardId;

    /**
     * 처리 유형 (approve: 승인, reject: 거부)
     */
    private String action;

    /**
     * 처리 사유 (선택사항)
     */
    private String reason;
}
