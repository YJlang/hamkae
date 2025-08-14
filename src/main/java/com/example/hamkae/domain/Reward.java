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
 * 상품권 교환을 관리하는 엔티티 클래스
 * 사용자가 포인트를 상품권으로 교환하는 요청을 관리합니다.
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
@Table(name = "rewards")
public class Reward {

    /**
     * 상품권 교환 요청 고유 식별자 (Primary Key)
     * 자동 증가하는 Long 타입
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 상품권 교환을 요청한 사용자
     * User 엔티티와 N:1 관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 교환에 사용된 포인트 수량
     * 교환 시점에 차감된 포인트
     */
    @Column(nullable = false)
    private Integer pointsUsed;

    /**
     * 교환할 상품권 타입
     * 예: "온누리상품권", "시장상품권" 등
     */
    @Column(nullable = false, length = 50)
    private String rewardType;

    /**
     * 교환 요청의 처리 상태
     * pending: 대기중
     * approved: 승인됨
     * rejected: 거부됨
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RewardStatus status = RewardStatus.PENDING;

    /**
     * 교환 요청일시
     * 자동 생성되며 수정 불가
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 교환 처리 완료일시
     * 승인/거부 시 설정
     */
    @Column
    private LocalDateTime processedAt;

    /**
     * 상품권 교환 상태를 나타내는 열거형
     */
    public enum RewardStatus {
        PENDING,    // 대기중
        APPROVED,   // 승인됨
        REJECTED    // 거부됨
    }

    /**
     * 교환 요청을 승인하는 메서드
     */
    public void approve() {
        this.status = RewardStatus.APPROVED;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * 교환 요청을 거부하는 메서드
     */
    public void reject() {
        this.status = RewardStatus.REJECTED;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * 교환 요청이 대기 중인지 확인하는 메서드
     * 
     * @return 대기 중이면 true
     */
    public boolean isPending() {
        return this.status == RewardStatus.PENDING;
    }

    /**
     * 교환 요청이 승인되었는지 확인하는 메서드
     * 
     * @return 승인되었으면 true
     */
    public boolean isApproved() {
        return this.status == RewardStatus.APPROVED;
    }

    /**
     * 교환 요청이 거부되었는지 확인하는 메서드
     * 
     * @return 거부되었으면 true
     */
    public boolean isRejected() {
        return this.status == RewardStatus.REJECTED;
    }

    /**
     * 교환 요청이 처리 완료되었는지 확인하는 메서드
     * 
     * @return 승인 또는 거부되었으면 true
     */
    public boolean isProcessed() {
        return this.status != RewardStatus.PENDING;
    }
}
