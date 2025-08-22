package com.example.hamkae.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * 상품권 핀번호를 관리하는 엔티티 클래스
 * 상품권 교환 승인 시 발급되는 핀번호를 관리합니다.
 *
 * @author 윤준하
 * @version 1.0
 * @since 2025-01-14
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "reward_pins")
public class RewardPin {

    /**
     * 핀번호 고유 식별자 (Primary Key)
     * 자동 증가하는 Long 타입
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 연결된 상품권 교환 요청
     * Reward 엔티티와 1:1 관계
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id", nullable = false, unique = true)
    private Reward reward;

    /**
     * 16자리 핀번호 (XXXX-XXXX-XXXX-XXXX 형식)
     * 상품권 사용 시 필요한 핀번호
     */
    @Column(nullable = false, unique = true, length = 19)
    private String pinNumber;

    /**
     * 핀번호 발급일시
     * 자동 생성되며 수정 불가
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    /**
     * 핀번호 만료일시
     * 발급일로부터 1년 후
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * 핀번호 사용 여부
     * true: 사용됨, false: 미사용
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isUsed = false;

    /**
     * 핀번호 사용일시
     * 사용 시 설정
     */
    @Column
    private LocalDateTime usedAt;

    /**
     * 16자리 핀번호를 생성하는 정적 팩토리 메서드
     * 
     * @param reward 연결될 상품권 교환 요청
     * @return 생성된 RewardPin 객체
     */
    public static RewardPin generatePin(Reward reward) {
        String pinNumber = generateRandomPin();
        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDateTime expiresAt = issuedAt.plusYears(1); // 1년 후 만료
        
        return RewardPin.builder()
                .reward(reward)
                .pinNumber(pinNumber)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .isUsed(false)
                .build();
    }

    /**
     * 16자리 랜덤 핀번호를 생성합니다.
     * 형식: XXXX-XXXX-XXXX-XXXX
     * 
     * @return 생성된 핀번호
     */
    private static String generateRandomPin() {
        Random random = new Random();
        StringBuilder pin = new StringBuilder();
        
        for (int i = 0; i < 4; i++) {
            if (i > 0) {
                pin.append("-");
            }
            // 4자리 숫자 생성 (0000-9999)
            int segment = random.nextInt(10000);
            pin.append(String.format("%04d", segment));
        }
        
        return pin.toString();
    }

    /**
     * 핀번호를 사용 처리합니다.
     */
    public void markAsUsed() {
        this.isUsed = true;
        this.usedAt = LocalDateTime.now();
    }

    /**
     * 핀번호가 만료되었는지 확인합니다.
     * 
     * @return 만료되었으면 true
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * 핀번호가 사용 가능한지 확인합니다.
     * 
     * @return 사용 가능하면 true (미사용이면서 만료되지 않음)
     */
    public boolean isAvailable() {
        return !this.isUsed && !isExpired();
    }

    /**
     * 핀번호의 마스킹된 버전을 반환합니다.
     * 보안상 일부만 표시 (XXXX-XXXX-XXXX-1234 형식)
     * 
     * @return 마스킹된 핀번호
     */
    public String getMaskedPinNumber() {
        if (pinNumber == null || pinNumber.length() < 19) {
            return "****-****-****-****";
        }
        
        String lastSegment = pinNumber.substring(15); // 마지막 4자리
        return "****-****-****-" + lastSegment;
    }

    /**
     * 상품권 타입을 반환합니다.
     * 
     * @return 상품권 타입
     */
    public String getRewardType() {
        return this.reward != null ? this.reward.getRewardType() : null;
    }

    /**
     * 사용된 포인트를 반환합니다.
     * 
     * @return 사용된 포인트
     */
    public Integer getPointsUsed() {
        return this.reward != null ? this.reward.getPointsUsed() : null;
    }

    /**
     * 핀번호 소유자를 반환합니다.
     * 
     * @return 핀번호 소유자
     */
    public User getUser() {
        return this.reward != null ? this.reward.getUser() : null;
    }
}
