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
 * @since 2025-08-13
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
     * 사용자 이름
     */
    private String name;

    /**
     * 사용자 아이디
     */
    private String username;

    /**
     * 보유 포인트
     */
    private Integer points;

    /**
     * 계정 생성일시
     */
    private LocalDateTime createdAt;

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
                .createdAt(user.getCreatedAt())
                .build();
    }
}
