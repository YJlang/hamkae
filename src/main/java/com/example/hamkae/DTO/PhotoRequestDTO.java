package com.example.hamkae.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사진 업로드를 위한 요청 DTO
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoRequestDTO {

    /**
     * 사진이 연결될 마커의 ID
     */
    private Long markerId;

    /**
     * 사진의 타입
     * report: 제보용 사진
     * before: 청소 전 사진
     * after: 청소 후 사진
     */
    private String type;

    /**
     * 이미지 파일 (MultipartFile로 처리)
     * 실제 구현 시에는 @RequestParam으로 받음
     */
    // private MultipartFile image;
}
