package com.example.hamkae.repository;

import com.example.hamkae.domain.Reward;
import com.example.hamkae.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 상품권 교환 정보를 데이터베이스에서 조회/저장하는 Repository 인터페이스
 * JpaRepository를 상속받아 기본적인 CRUD 기능을 제공합니다.
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-01-14
 */
public interface RewardRepository extends JpaRepository<Reward, Long> {
    
    /**
     * 특정 사용자의 상품권 교환 요청을 최신순으로 조회합니다.
     * 
     * @param user 조회할 사용자
     * @return 상품권 교환 요청 목록 (최신순)
     */
    List<Reward> findByUserOrderByCreatedAtDesc(User user);

    /**
     * 특정 상태의 상품권 교환 요청을 최신순으로 조회합니다.
     * 
     * @param status 상품권 교환 상태
     * @return 해당 상태의 상품권 교환 요청 목록
     */
    List<Reward> findByStatusOrderByCreatedAtDesc(Reward.RewardStatus status);

    /**
     * 특정 사용자의 특정 상태 상품권 교환 요청을 조회합니다.
     * 
     * @param user 조회할 사용자
     * @param status 상품권 교환 상태
     * @return 해당 조건의 상품권 교환 요청 목록
     */
    List<Reward> findByUserAndStatusOrderByCreatedAtDesc(User user, Reward.RewardStatus status);

    /**
     * 특정 기간 동안의 상품권 교환 요청을 조회합니다.
     * 
     * @param startDate 시작일시
     * @param endDate 종료일시
     * @return 해당 기간의 상품권 교환 요청 목록
     */
    List<Reward> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 특정 사용자의 총 사용 포인트를 계산합니다 (승인된 요청만).
     * 
     * @param user 조회할 사용자
     * @return 총 사용 포인트 (null일 경우 0)
     */
    @Query("SELECT COALESCE(SUM(r.pointsUsed), 0) FROM Reward r WHERE r.user = :user AND r.status = 'APPROVED'")
    Integer getTotalUsedPointsByUser(@Param("user") User user);

    /**
     * 특정 상품권 타입별 교환 횟수를 조회합니다.
     * 
     * @param rewardType 상품권 타입
     * @return 해당 타입의 교환 횟수
     */
    Long countByRewardTypeAndStatus(String rewardType, Reward.RewardStatus status);

    /**
     * 특정 사용자의 대기중인 상품권 교환 요청을 조회합니다.
     * 
     * @param user 조회할 사용자
     * @return 대기중인 상품권 교환 요청 목록
     */
    @Query("SELECT r FROM Reward r WHERE r.user = :user AND r.status = 'PENDING' ORDER BY r.createdAt DESC")
    List<Reward> findPendingRewardsByUser(@Param("user") User user);

    /**
     * 관리자용: 처리 대기중인 모든 상품권 교환 요청을 조회합니다.
     * 
     * @return 대기중인 모든 상품권 교환 요청 목록
     */
    @Query("SELECT r FROM Reward r WHERE r.status = 'PENDING' ORDER BY r.createdAt ASC")
    List<Reward> findAllPendingRewards();

    /**
     * 특정 사용자의 월별 상품권 교환 통계를 조회합니다.
     * 
     * @param user 조회할 사용자
     * @param year 조회할 연도
     * @param month 조회할 월
     * @return 해당 월의 총 사용 포인트
     */
    @Query("SELECT COALESCE(SUM(r.pointsUsed), 0) FROM Reward r WHERE r.user = :user AND r.status = 'APPROVED' " +
           "AND YEAR(r.createdAt) = :year AND MONTH(r.createdAt) = :month")
    Integer getMonthlyUsedPoints(@Param("user") User user, @Param("year") int year, @Param("month") int month);
}
