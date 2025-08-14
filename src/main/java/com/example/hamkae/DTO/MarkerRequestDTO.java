package com.example.hamkae.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 마커 등록을 위한 요청 DTO
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkerRequestDTO {

    /**
     * 마커의 위도 좌표
     * 정밀도: 소수점 8자리까지
     */
    private BigDecimal lat;

    /**
     * 마커의 경도 좌표
     * 정밀도: 소수점 8자리까지
     */
    private BigDecimal lng;

    /**
     * 쓰레기 위치에 대한 설명
     * 예: "공원 입구 쓰레기통 옆", "주차장 구석" 등
     */
    private String description;
}
