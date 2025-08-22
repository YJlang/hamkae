package com.example.hamkae.DTO;

import com.example.hamkae.domain.RewardPin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상품권 핀번호 조회를 위한 응답 DTO
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardPinResponseDTO {

    /**
     * 핀번호 고유 식별자
     */
    private Long id;

    /**
     * 연결된 상품권 교환 요청 ID
     */
    private Long rewardId;

    /**
     * 핀번호 (보안상 마스킹된 버전)
     */
    private String maskedPinNumber;

    /**
     * 실제 핀번호 (특별한 경우에만 포함)
     */
    private String fullPinNumber;

    /**
     * 상품권 타입
     */
    private String rewardType;

    /**
     * 사용된 포인트
     */
    private Integer pointsUsed;

    /**
     * 핀번호 발급일시
     */
    private LocalDateTime issuedAt;

    /**
     * 핀번호 만료일시
     */
    private LocalDateTime expiresAt;

    /**
     * 핀번호 사용 여부
     */
    private Boolean isUsed;

    /**
     * 핀번호 사용일시
     */
    private LocalDateTime usedAt;

    /**
     * 핀번호 사용 가능 여부
     */
    private Boolean isAvailable;

    /**
     * 핀번호 만료 여부
     */
    private Boolean isExpired;

    /**
     * RewardPin 엔티티를 RewardPinResponseDTO로 변환하는 정적 팩토리 메서드 (마스킹된 버전)
     * 
     * @param rewardPin 변환할 RewardPin 엔티티
     * @return RewardPinResponseDTO 객체
     */
    public static RewardPinResponseDTO from(RewardPin rewardPin) {
        return RewardPinResponseDTO.builder()
                .id(rewardPin.getId())
                .rewardId(rewardPin.getReward().getId())
                .maskedPinNumber(rewardPin.getMaskedPinNumber())
                .rewardType(rewardPin.getRewardType())
                .pointsUsed(rewardPin.getPointsUsed())
                .issuedAt(rewardPin.getIssuedAt())
                .expiresAt(rewardPin.getExpiresAt())
                .isUsed(rewardPin.getIsUsed())
                .usedAt(rewardPin.getUsedAt())
                .isAvailable(rewardPin.isAvailable())
                .isExpired(rewardPin.isExpired())
                .build();
    }

    /**
     * 실제 핀번호를 포함한 전체 정보로 변환하는 정적 팩토리 메서드
     * 주의: 보안에 민감하므로 특별한 경우에만 사용
     * 
     * @param rewardPin 변환할 RewardPin 엔티티
     * @return 실제 핀번호를 포함한 RewardPinResponseDTO 객체
     */
    public static RewardPinResponseDTO withFullPin(RewardPin rewardPin) {
        RewardPinResponseDTO dto = from(rewardPin);
        dto.setFullPinNumber(rewardPin.getPinNumber());
        return dto;
    }

    /**
     * 핀번호 발급 성공 시 사용할 정적 팩토리 메서드
     * 새로 발급된 핀번호의 전체 정보를 제공
     * 
     * @param rewardPin 새로 발급된 RewardPin 엔티티
     * @return 실제 핀번호를 포함한 RewardPinResponseDTO 객체
     */
    public static RewardPinResponseDTO forNewIssue(RewardPin rewardPin) {
        return withFullPin(rewardPin);
    }
}
