package com.example.hamkae.DTO;

import com.example.hamkae.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 프로필 조회를 위한 응답 DTO
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponseDTO {

    /**
     * 사용자 고유 식별자
     */
    private Long id;

    /**
     * 사용자 실명
     */
    private String name;

    /**
     * 사용자 아이디
     */
    private String username;

    /**
     * 현재 보유 포인트
     */
    private Integer points;

    /**
     * 총 적립 포인트
     */
    private Integer totalEarnedPoints;

    /**
     * 총 사용 포인트
     */
    private Integer totalUsedPoints;

    /**
     * 제보한 마커 수
     */
    private Integer reportedMarkersCount;

    /**
     * 업로드한 사진 수
     */
    private Integer uploadedPhotosCount;

    /**
     * 상품권 교환 횟수
     */
    private Integer rewardExchangeCount;

    /**
     * 발급받은 핀번호 수
     */
    private Integer issuedPinsCount;

    /**
     * 계정 생성일시
     */
    private LocalDateTime createdAt;

    /**
     * 계정 정보 수정일시
     */
    private LocalDateTime updatedAt;

    /**
     * User 엔티티를 UserProfileResponseDTO로 변환하는 정적 팩토리 메서드
     * 
     * @param user 변환할 User 엔티티
     * @return UserProfileResponseDTO 객체
     */
    public static UserProfileResponseDTO from(User user) {
        return UserProfileResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .points(user.getPoints())
                .reportedMarkersCount(user.getReportedMarkers() != null ? user.getReportedMarkers().size() : 0)
                .uploadedPhotosCount(user.getUploadedPhotos() != null ? user.getUploadedPhotos().size() : 0)
                .rewardExchangeCount(user.getRewards() != null ? user.getRewards().size() : 0)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * 포인트 통계를 포함한 UserProfileResponseDTO를 생성하는 정적 팩토리 메서드
     * 
     * @param user 변환할 User 엔티티
     * @param totalEarnedPoints 총 적립 포인트
     * @param totalUsedPoints 총 사용 포인트
     * @param issuedPinsCount 발급받은 핀번호 수
     * @return 포인트 통계를 포함한 UserProfileResponseDTO 객체
     */
    public static UserProfileResponseDTO withPointStatistics(User user, Integer totalEarnedPoints, 
                                                           Integer totalUsedPoints, Integer issuedPinsCount) {
        UserProfileResponseDTO dto = from(user);
        dto.setTotalEarnedPoints(totalEarnedPoints);
        dto.setTotalUsedPoints(totalUsedPoints);
        dto.setIssuedPinsCount(issuedPinsCount);
        return dto;
    }
}