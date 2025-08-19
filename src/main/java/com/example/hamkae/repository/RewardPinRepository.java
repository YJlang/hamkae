package com.example.hamkae.repository;

import com.example.hamkae.domain.Reward;
import com.example.hamkae.domain.RewardPin;
import com.example.hamkae.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 상품권 핀번호 정보를 데이터베이스에서 조회/저장하는 Repository 인터페이스
 * JpaRepository를 상속받아 기본적인 CRUD 기능을 제공합니다.
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-01-14
 */
public interface RewardPinRepository extends JpaRepository<RewardPin, Long> {
    
    /**
     * 특정 상품권 교환 요청의 핀번호를 조회합니다.
     * 
     * @param reward 조회할 상품권 교환 요청
     * @return 핀번호 정보 (Optional)
     */
    Optional<RewardPin> findByReward(Reward reward);

    /**
     * 상품권 교환 요청 ID로 핀번호를 조회합니다.
     * 
     * @param rewardId 상품권 교환 요청 ID
     * @return 핀번호 정보 (Optional)
     */
    Optional<RewardPin> findByRewardId(Long rewardId);

    /**
     * 핀번호로 상품권 핀 정보를 조회합니다.
     * 
     * @param pinNumber 조회할 핀번호
     * @return 핀번호 정보 (Optional)
     */
    Optional<RewardPin> findByPinNumber(String pinNumber);

    /**
     * 특정 사용자의 모든 핀번호를 최신순으로 조회합니다.
     * 
     * @param user 조회할 사용자
     * @return 핀번호 목록 (최신순)
     */
    @Query("SELECT rp FROM RewardPin rp WHERE rp.reward.user = :user ORDER BY rp.issuedAt DESC")
    List<RewardPin> findByUserOrderByIssuedAtDesc(@Param("user") User user);

    /**
     * 특정 사용자의 사용 가능한 핀번호를 조회합니다.
     * 
     * @param user 조회할 사용자
     * @return 사용 가능한 핀번호 목록
     */
    @Query("SELECT rp FROM RewardPin rp WHERE rp.reward.user = :user AND rp.isUsed = false AND rp.expiresAt > :now ORDER BY rp.issuedAt DESC")
    List<RewardPin> findAvailablePinsByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * 특정 사용자의 사용된 핀번호를 조회합니다.
     * 
     * @param user 조회할 사용자
     * @return 사용된 핀번호 목록
     */
    @Query("SELECT rp FROM RewardPin rp WHERE rp.reward.user = :user AND rp.isUsed = true ORDER BY rp.usedAt DESC")
    List<RewardPin> findUsedPinsByUser(@Param("user") User user);

    /**
     * 만료된 핀번호를 조회합니다.
     * 
     * @param now 현재 시간
     * @return 만료된 핀번호 목록
     */
    @Query("SELECT rp FROM RewardPin rp WHERE rp.isUsed = false AND rp.expiresAt <= :now")
    List<RewardPin> findExpiredPins(@Param("now") LocalDateTime now);

    /**
     * 특정 기간에 발급된 핀번호를 조회합니다.
     * 
     * @param startDate 시작일시
     * @param endDate 종료일시
     * @return 해당 기간에 발급된 핀번호 목록
     */
    List<RewardPin> findByIssuedAtBetweenOrderByIssuedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 특정 상품권 타입의 핀번호 발급 수를 조회합니다.
     * 
     * @param rewardType 상품권 타입
     * @return 발급 수
     */
    @Query("SELECT COUNT(rp) FROM RewardPin rp WHERE rp.reward.rewardType = :rewardType")
    Long countByRewardType(@Param("rewardType") String rewardType);

    /**
     * 특정 사용자의 특정 상품권 타입 핀번호를 조회합니다.
     * 
     * @param user 조회할 사용자
     * @param rewardType 상품권 타입
     * @return 해당 조건의 핀번호 목록
     */
    @Query("SELECT rp FROM RewardPin rp WHERE rp.reward.user = :user AND rp.reward.rewardType = :rewardType ORDER BY rp.issuedAt DESC")
    List<RewardPin> findByUserAndRewardType(@Param("user") User user, @Param("rewardType") String rewardType);

    /**
     * 핀번호가 이미 존재하는지 확인합니다.
     * 
     * @param pinNumber 확인할 핀번호
     * @return 존재하면 true
     */
    boolean existsByPinNumber(String pinNumber);

    /**
     * 상품권 교환 요청에 대한 핀번호가 이미 발급되었는지 확인합니다.
     * 
     * @param reward 확인할 상품권 교환 요청
     * @return 발급되었으면 true
     */
    boolean existsByReward(Reward reward);

    /**
     * 특정 사용자의 총 핀번호 발급 수를 조회합니다.
     * 
     * @param user 조회할 사용자
     * @return 총 발급 수
     */
    @Query("SELECT COUNT(rp) FROM RewardPin rp WHERE rp.reward.user = :user")
    Long countByUser(@Param("user") User user);
}
