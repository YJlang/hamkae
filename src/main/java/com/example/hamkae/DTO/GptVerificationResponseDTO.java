package com.example.hamkae.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GPT API 검증 응답을 위한 DTO
 * GPT API로부터 받은 검증 결과를 담습니다.
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GptVerificationResponseDTO {

    /**
     * 검증 성공 여부
     */
    private boolean success;

    /**
     * 검증 결과
     * APPROVED: 청소 완료로 인정
     * REJECTED: 청소 미완료로 판단
     */
    private String verificationResult;

    /**
     * GPT API의 상세 응답 내용
     */
    private String gptResponse;

    /**
     * 검증 신뢰도 (0.0 ~ 1.0)
     */
    private Double confidence;

    /**
     * 검증 실패 시 에러 메시지
     */
    private String errorMessage;

    /**
     * 검증 완료 시점의 타임스탬프
     */
    private String verifiedAt;
}
