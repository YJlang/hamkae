package com.example.hamkae.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 쓰레기 위치 마커를 관리하는 엔티티 클래스
 * 사용자가 제보한 쓰레기 위치 정보를 저장합니다.
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
@Table(name = "markers")
public class Marker {

    /**
     * 마커 고유 식별자 (Primary Key)
     * 자동 증가하는 Long 타입
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 마커의 위도 좌표
     * 정밀도: 소수점 8자리까지
     */
    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal lat;

    /**
     * 마커의 경도 좌표
     * 정밀도: 소수점 8자리까지
     */
    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal lng;

    /**
     * 쓰레기 위치에 대한 설명
     * 예: "공원 입구 쓰레기통 옆", "주차장 구석" 등
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 쓰레기 위치의 실제 주소
     * 예: "서울시 강남구 테헤란로 123", "경기도 성남시 분당구 정자로 123" 등
     */
    @Column(columnDefinition = "TEXT")
    private String address;

    /**
     * 마커의 현재 상태
     * active: 활성 상태 (청소 대기)
     * cleaned: 청소 완료
     * removed: 삭제됨
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MarkerStatus status = MarkerStatus.ACTIVE;

    /**
     * 마커를 제보한 사용자
     * User 엔티티와 N:1 관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by", nullable = false)
    private User reportedBy;

    /**
     * 마커에 연결된 사진들
     * 1:N 관계, 마커 삭제 시 사진도 함께 삭제
     */
    @OneToMany(mappedBy = "marker", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Photo> photos = new ArrayList<>();

    /**
     * 마커 등록일시
     * 자동 생성되며 수정 불가
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 마커 정보 수정일시
     * 엔티티 수정 시 자동 업데이트
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 마커 상태를 나타내는 열거형
     */
    public enum MarkerStatus {
        ACTIVE,     // 활성 상태 (청소 대기)
        CLEANED,    // 청소 완료
        REMOVED     // 삭제됨
    }

    // ========== Setter 메서드 (양방향 관계 설정용) ==========

    /**
     * 제보자를 설정하는 메서드 (양방향 관계 설정용)
     * 
     * @param reportedBy 제보자
     */
    public void setReportedBy(User reportedBy) {
        this.reportedBy = reportedBy;
    }

    // ========== 비즈니스 메서드 ==========

    /**
     * 마커에 사진을 추가하는 메서드
     * 
     * @param photo 추가할 사진
     */
    public void addPhoto(Photo photo) {
        this.photos.add(photo);
        photo.setMarker(this);
    }

    /**
     * 마커에서 사진을 제거하는 메서드
     * 
     * @param photo 제거할 사진
     */
    public void removePhoto(Photo photo) {
        this.photos.remove(photo);
        photo.setMarker(null);
    }

    /**
     * 마커 상태를 청소 완료로 변경하는 메서드
     */
    public void markAsCleaned() {
        this.status = MarkerStatus.CLEANED;
    }

    /**
     * 마커 상태를 삭제됨으로 변경하는 메서드
     */
    public void markAsRemoved() {
        this.status = MarkerStatus.REMOVED;
    }

    /**
     * 마커가 활성 상태인지 확인하는 메서드
     * 
     * @return 활성 상태이면 true
     */
    public boolean isActive() {
        return this.status == MarkerStatus.ACTIVE;
    }

    /**
     * 마커가 청소 완료되었는지 확인하는 메서드
     * 
     * @return 청소 완료되었으면 true
     */
    public boolean isCleaned() {
        return this.status == MarkerStatus.CLEANED;
    }

    /**
     * 마커가 삭제되었는지 확인하는 메서드
     * 
     * @return 삭제되었으면 true
     */
    public boolean isRemoved() {
        return this.status == MarkerStatus.REMOVED;
    }

    /**
     * 마커에 제보 사진이 있는지 확인하는 메서드 (BEFORE 타입과 동일)
     * 
     * @return 제보 사진이 있으면 true
     */
    public boolean hasReportPhoto() {
        return this.photos.stream()
                .anyMatch(photo -> photo.getType() == Photo.PhotoType.BEFORE);
    }

    /**
     * 마커에 청소 전 사진이 있는지 확인하는 메서드
     * 
     * @return 청소 전 사진이 있으면 true
     */
    public boolean hasBeforePhoto() {
        return this.photos.stream()
                .anyMatch(photo -> photo.getType() == Photo.PhotoType.BEFORE);
    }

    /**
     * 마커에 청소 후 사진이 있는지 확인하는 메서드
     * 
     * @return 청소 후 사진이 있으면 true
     */
    public boolean hasAfterPhoto() {
        return this.photos.stream()
                .anyMatch(photo -> photo.getType() == Photo.PhotoType.AFTER);
    }
}
