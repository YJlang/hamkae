package com.example.hamkae.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 포인트 적립/사용 이력을 관리하는 엔티티 클래스
 * 사용자의 포인트 변동 내역을 추적합니다.
 *
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-13
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "point_history")
public class PointHistory {

    /**
     * 포인트 이력 고유 식별자 (Primary Key)
     * 자동 증가하는 Long 타입
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 포인트를 적립/사용한 사용자
     * User 엔티티와 N:1 관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 변동된 포인트 수량
     * 양수: 적립, 음수: 사용
     */
    @Column(nullable = false)
    private Integer points;

    /**
     * 포인트 변동 타입
     * earned: 적립, used: 사용
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PointType type;

    /**
     * 포인트 변동 사유
     * 예: "청소 인증 완료", "상품권 교환" 등
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 포인트 적립과 관련된 사진 (적립 시에만)
     * Photo 엔티티와 N:1 관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_photo_id")
    private Photo relatedPhoto;

    /**
     * 포인트 변동일시
     * 자동 생성되며 수정 불가
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 포인트 타입을 나타내는 열거형
     */
    public enum PointType {
        EARNED,     // 적립
        USED        // 사용
    }

    /**
     * 포인트 적립 이력을 생성하는 정적 팩토리 메서드
     * 
     * @param user 포인트를 적립할 사용자
     * @param points 적립할 포인트 수량
     * @param description 적립 사유
     * @param relatedPhoto 관련 사진
     * @return 포인트 적립 이력 객체
     */
    public static PointHistory createEarnedHistory(User user, Integer points, String description, Photo relatedPhoto) {
        return PointHistory.builder()
                .user(user)
                .points(points)
                .type(PointType.EARNED)
                .description(description)
                .relatedPhoto(relatedPhoto)
                .build();
    }

    /**
     * 포인트 사용 이력을 생성하는 정적 팩토리 메서드
     * 
     * @param user 포인트를 사용할 사용자
     * @param points 사용할 포인트 수량
     * @param description 사용 사유
     * @return 포인트 사용 이력 객체
     */
    public static PointHistory createUsedHistory(User user, Integer points, String description) {
        return PointHistory.builder()
                .user(user)
                .points(-points) // 음수로 저장
                .type(PointType.USED)
                .description(description)
                .build();
    }

    /**
     * 포인트가 적립인지 확인하는 메서드
     * 
     * @return 적립이면 true
     */
    public boolean isEarned() {
        return this.type == PointType.EARNED;
    }

    /**
     * 포인트가 사용인지 확인하는 메서드
     * 
     * @return 사용이면 true
     */
    public boolean isUsed() {
        return this.type == PointType.USED;
    }

    /**
     * 절댓값 포인트를 반환하는 메서드
     * 
     * @return 포인트의 절댓값
     */
    public Integer getAbsolutePoints() {
        return Math.abs(this.points);
    }
}
