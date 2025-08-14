package com.example.hamkae.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GPT API 검증 요청을 위한 DTO
 * 사진 비교 검증을 위해 GPT API에 전달할 데이터를 담습니다.
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GptVerificationRequestDTO {

    /**
     * 청소 전 사진의 Base64 인코딩된 이미지 데이터
     */
    private String beforeImageBase64;

    /**
     * 청소 후 사진의 Base64 인코딩된 이미지 데이터
     */
    private String afterImageBase64;

    /**
     * 마커 위치 설명 (GPT가 더 정확한 판단을 위해 사용)
     */
    private String locationDescription;

    /**
     * 검증 요청 시점의 타임스탬프
     */
    private String timestamp;
}
