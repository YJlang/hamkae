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
 * 사진 업로드 및 관리를 위한 엔티티 클래스
 * 제보용, 청소 전/후 사진을 관리합니다.
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
@Table(name = "photos")
public class Photo {

    /**
     * 사진 고유 식별자 (Primary Key)
     * 자동 증가하는 Long 타입
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사진이 연결된 마커
     * Marker 엔티티와 N:1 관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marker_id", nullable = false)
    private Marker marker;

    /**
     * 사진을 업로드한 사용자
     * User 엔티티와 N:1 관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 이미지 파일 경로
     * 로컬 저장소 또는 S3 등의 경로
     */
    @Column(nullable = false, length = 500)
    private String imagePath;

    /**
     * 사진의 타입
     * report: 제보용 사진
     * before: 청소 전 사진
     * after: 청소 후 사진
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PhotoType type;

    /**
     * AI 검증 상태
     * pending: 대기중
     * approved: 승인됨
     * rejected: 거부됨
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    /**
     * GPT API 응답 결과
     * AI 검증 후 반환된 결과를 저장
     */
    @Column(columnDefinition = "TEXT")
    private String gptResponse;

    /**
     * AI 검증 완료 시점
     * AI 검증이 완료된 시점을 저장
     */
    @Column
    private LocalDateTime verifiedAt;

    /**
     * 사진 업로드일시
     * 자동 생성되며 수정 불가
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 사진 타입을 나타내는 열거형
     */
    public enum PhotoType {
        BEFORE,     // 청소 전 사진 (쓰레기 제보용)
        AFTER       // 청소 후 사진 (청소 인증용)
    }

    /**
     * 검증 상태를 나타내는 열거형
     */
    public enum VerificationStatus {
        PENDING,    // 대기중
        APPROVED,   // 승인됨
        REJECTED    // 거부됨
    }

    // ========== Setter 메서드 (양방향 관계 설정용) ==========

    /**
     * 마커를 설정하는 메서드 (양방향 관계 설정용)
     * 
     * @param marker 설정할 마커
     */
    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    /**
     * 사용자를 설정하는 메서드 (양방향 관계 설정용)
     * 
     * @param user 설정할 사용자
     */
    public void setUser(User user) {
        this.user = user;
    }

    // ========== 비즈니스 메서드 ==========

    /**
     * 검증 상태를 승인으로 변경하는 메서드
     * 
     * @param gptResponse GPT API 응답 결과
     */
    public void approve(String gptResponse) {
        this.verificationStatus = VerificationStatus.APPROVED;
        this.gptResponse = gptResponse;
        this.verifiedAt = LocalDateTime.now(); // AI 검증 완료 시점 설정
    }

    /**
     * 검증 상태를 거부로 변경하는 메서드
     * 
     * @param gptResponse GPT API 응답 결과
     */
    public void reject(String gptResponse) {
        this.verificationStatus = VerificationStatus.REJECTED;
        this.gptResponse = gptResponse;
        this.verifiedAt = LocalDateTime.now(); // AI 검증 완료 시점 설정
    }

    /**
     * 사진이 청소 인증용인지 확인하는 메서드
     * 
     * @return 청소 인증용 사진이면 true
     */
    public boolean isCleanupVerification() {
        return this.type == PhotoType.AFTER;
    }

    /**
     * 사진이 제보용인지 확인하는 메서드 (BEFORE 타입과 동일)
     * 
     * @return 제보용 사진이면 true
     */
    public boolean isReport() {
        return this.type == PhotoType.BEFORE;
    }

    /**
     * 사진이 청소 전인지 확인하는 메서드
     * 
     * @return 청소 전 사진이면 true
     */
    public boolean isBefore() {
        return this.type == PhotoType.BEFORE;
    }

    /**
     * 사진이 청소 후인지 확인하는 메서드
     * 
     * @return 청소 후 사진이면 true
     */
    public boolean isAfter() {
        return this.type == PhotoType.AFTER;
    }

    /**
     * 사진이 검증 대기 중인지 확인하는 메서드
     * 
     * @return 검증 대기 중이면 true
     */
    public boolean isPending() {
        return this.verificationStatus == VerificationStatus.PENDING;
    }

    /**
     * 사진이 검증 승인되었는지 확인하는 메서드
     * 
     * @return 검증 승인되었으면 true
     */
    public boolean isApproved() {
        return this.verificationStatus == VerificationStatus.APPROVED;
    }

    /**
     * 사진이 검증 거부되었는지 확인하는 메서드
     * 
     * @return 검증 거부되었으면 true
     */
    public boolean isRejected() {
        return this.verificationStatus == VerificationStatus.REJECTED;
    }

    /**
     * 사진이 검증 완료되었는지 확인하는 메서드
     * 
     * @return 검증 완료되었으면 true
     */
    public boolean isVerified() {
        return this.verificationStatus != VerificationStatus.PENDING;
    }
}
