package com.example.hamkae.repository;

import com.example.hamkae.domain.PointHistory;
import com.example.hamkae.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 포인트 이력 정보를 데이터베이스에서 조회/저장하는 Repository 인터페이스
 * JpaRepository를 상속받아 기본적인 CRUD 기능을 제공합니다.
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-20
 */
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    
    /**
     * 특정 사용자의 포인트 이력을 최신순으로 조회합니다.
     * 
     * @param user 조회할 사용자
     * @return 포인트 이력 목록 (최신순)
     */
    List<PointHistory> findByUserOrderByCreatedAtDesc(User user);

    /**
     * 특정 사용자의 포인트 이력을 타입별로 조회합니다.
     * 
     * @param user 조회할 사용자
     * @param type 포인트 타입 (EARNED/USED)
     * @return 해당 타입의 포인트 이력 목록
     */
    List<PointHistory> findByUserAndTypeOrderByCreatedAtDesc(User user, PointHistory.PointType type);

    /**
     * 특정 기간 동안의 사용자 포인트 이력을 조회합니다.
     * 
     * @param user 조회할 사용자
     * @param startDate 시작일시
     * @param endDate 종료일시
     * @return 해당 기간의 포인트 이력 목록
     */
    List<PointHistory> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(
            User user, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 특정 사용자의 총 적립 포인트를 계산합니다.
     * 
     * @param user 조회할 사용자
     * @return 총 적립 포인트 (null일 경우 0)
     */
    @Query("SELECT COALESCE(SUM(p.points), 0) FROM PointHistory p WHERE p.user = :user AND p.type = 'EARNED'")
    Integer getTotalEarnedPointsByUser(@Param("user") User user);

    /**
     * 특정 사용자의 총 사용 포인트를 계산합니다.
     * 
     * @param user 조회할 사용자
     * @return 총 사용 포인트 (절댓값, null일 경우 0)
     */
    @Query("SELECT COALESCE(SUM(ABS(p.points)), 0) FROM PointHistory p WHERE p.user = :user AND p.type = 'USED'")
    Integer getTotalUsedPointsByUser(@Param("user") User user);

    /**
     * 특정 사용자의 월별 포인트 적립 통계를 조회합니다.
     * 
     * @param user 조회할 사용자
     * @param year 조회할 연도
     * @param month 조회할 월
     * @return 해당 월의 총 적립 포인트
     */
    @Query("SELECT COALESCE(SUM(p.points), 0) FROM PointHistory p WHERE p.user = :user AND p.type = 'EARNED' " +
           "AND YEAR(p.createdAt) = :year AND MONTH(p.createdAt) = :month")
    Integer getMonthlyEarnedPoints(@Param("user") User user, @Param("year") int year, @Param("month") int month);

    /**
     * 특정 사용자의 최근 N개 포인트 이력을 조회합니다.
     * 
     * @param user 조회할 사용자
     * @param limit 조회할 개수
     * @return 최근 포인트 이력 목록
     */
    @Query("SELECT p FROM PointHistory p WHERE p.user = :user ORDER BY p.createdAt DESC LIMIT :limit")
    List<PointHistory> findRecentPointHistories(@Param("user") User user, @Param("limit") int limit);
}
