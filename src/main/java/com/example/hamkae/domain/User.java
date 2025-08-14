package com.example.hamkae.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 사용자 정보를 담는 엔티티 클래스
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
@Table(name = "users")
public class User {

    /**
     * 사용자 고유 식별자 (Primary Key)
     * 자동 증가하는 Long 타입
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자 실명
     * 필수 입력 항목, 최대 100자
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 사용자 로그인 아이디
     * 고유값, 필수 입력 항목, 최대 100자
     */
    @Column(unique = true, nullable = false, length = 100)
    private String username;

    /**
     * 사용자 비밀번호
     * BCrypt로 암호화되어 저장, 필수 입력 항목
     */
    @Column(nullable = false)
    private String password;

    /**
     * 사용자 보유 포인트
     * 기본값 0, 정수형
     */
    @Column(columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer points = 0;

    /**
     * 계정 생성일시
     * 자동 생성되며 수정 불가
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 계정 정보 수정일시
     * 엔티티 수정 시 자동 업데이트
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ========== 관계 매핑 ==========

    /**
     * 사용자가 제보한 마커들
     * 1:N 관계, 사용자 삭제 시 마커도 함께 삭제
     */
    @OneToMany(mappedBy = "reportedBy", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Marker> reportedMarkers = new ArrayList<>();

    /**
     * 사용자가 업로드한 사진들
     * 1:N 관계, 사용자 삭제 시 사진도 함께 삭제
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Photo> uploadedPhotos = new ArrayList<>();

    /**
     * 사용자의 포인트 변동 이력
     * 1:N 관계, 사용자 삭제 시 이력도 함께 삭제
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PointHistory> pointHistories = new ArrayList<>();

    /**
     * 사용자의 상품권 교환 요청들
     * 1:N 관계, 사용자 삭제 시 요청도 함께 삭제
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Reward> rewards = new ArrayList<>();

    // ========== 비즈니스 메서드 ==========

    /**
     * 포인트를 적립하는 메서드
     * 
     * @param points 적립할 포인트 수량
     */
    public void addPoints(Integer points) {
        this.points += points;
    }

    /**
     * 포인트를 사용하는 메서드
     * 
     * @param points 사용할 포인트 수량
     * @throws IllegalStateException 보유 포인트가 부족한 경우
     */
    public void usePoints(Integer points) {
        if (this.points < points) {
            throw new IllegalStateException("보유 포인트가 부족합니다. 현재: " + this.points + ", 필요: " + points);
        }
        this.points -= points;
    }

    /**
     * 보유 포인트가 충분한지 확인하는 메서드
     * 
     * @param requiredPoints 필요한 포인트 수량
     * @return 충분하면 true
     */
    public boolean hasEnoughPoints(Integer requiredPoints) {
        return this.points >= requiredPoints;
    }

    /**
     * 마커를 제보하는 메서드
     * 
     * @param marker 제보할 마커
     */
    public void reportMarker(Marker marker) {
        this.reportedMarkers.add(marker);
        marker.setReportedBy(this);
    }

    /**
     * 사진을 업로드하는 메서드
     * 
     * @param photo 업로드할 사진
     */
    public void uploadPhoto(Photo photo) {
        this.uploadedPhotos.add(photo);
        photo.setUser(this);
    }

    /**
     * 포인트 이력을 추가하는 메서드
     * 
     * @param pointHistory 추가할 포인트 이력
     */
    public void addPointHistory(PointHistory pointHistory) {
        this.pointHistories.add(pointHistory);
    }

    /**
     * 상품권 교환 요청을 추가하는 메서드
     * 
     * @param reward 추가할 상품권 교환 요청
     */
    public void addReward(Reward reward) {
        this.rewards.add(reward);
    }

    /**
     * 사용자 이름을 변경합니다.
     * 
     * @param newName 새로운 사용자 이름
     */
    public void updateName(String newName) {
        this.name = newName;
    }
}