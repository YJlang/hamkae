package com.example.hamkae.DTO;

import com.example.hamkae.domain.Photo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사진 조회를 위한 응답 DTO
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoResponseDTO {

    /**
     * 사진 고유 식별자
     */
    private Long id;

    /**
     * 사진이 연결된 마커의 ID
     */
    private Long markerId;

    /**
     * 사진을 업로드한 사용자의 ID
     */
    private Long userId;

    /**
     * 이미지 파일 경로
     */
    private String imagePath;

    /**
     * 사진의 타입
     */
    private String type;

    /**
     * AI 검증 상태
     */
    private String verificationStatus;

    /**
     * GPT API 응답 결과
     */
    private String gptResponse;

    /**
     * 사진 업로드일시
     */
    private LocalDateTime createdAt;

    /**
     * Photo 엔티티를 PhotoResponseDTO로 변환하는 정적 팩토리 메서드
     * 
     * @param photo 변환할 Photo 엔티티
     * @return PhotoResponseDTO 객체
     */
    public static PhotoResponseDTO from(Photo photo) {
        return PhotoResponseDTO.builder()
                .id(photo.getId())
                .markerId(photo.getMarker().getId())
                .userId(photo.getUser().getId())
                .imagePath(photo.getImagePath())
                .type(photo.getType().name())
                .verificationStatus(photo.getVerificationStatus().name())
                .gptResponse(photo.getGptResponse())
                .createdAt(photo.getCreatedAt())
                .build();
    }
}
