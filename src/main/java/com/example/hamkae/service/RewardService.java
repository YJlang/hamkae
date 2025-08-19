package com.example.hamkae.service;

import com.example.hamkae.domain.Reward;
import com.example.hamkae.domain.User;
import com.example.hamkae.repository.RewardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 상품권 교환 관련 비즈니스 로직을 처리하는 서비스 클래스
 * 상품권 교환 요청, 승인, 거부 등의 기능을 제공합니다.
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-20
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RewardService {

    private final RewardRepository rewardRepository;
    private final PointHistoryService pointHistoryService;
    private final UserService userService;
    private final RewardPinService rewardPinService;

    /**
     * 상품권 교환을 즉시 처리합니다. (관리자 승인 과정 없이 바로 핀번호 발급)
     * 
     * @param user 교환을 요청하는 사용자
     * @param pointsToUse 사용할 포인트 수량
     * @param rewardType 교환할 상품권 타입
     * @return 처리된 상품권 교환 정보 (핀번호 포함)
     * @throws IllegalStateException 포인트가 부족한 경우
     */
    @Transactional
    public Reward exchangeRewardImmediately(User user, Integer pointsToUse, String rewardType) {
        log.info("상품권 즉시 교환: 사용자={}, 포인트={}, 타입={}", 
                user.getUsername(), pointsToUse, rewardType);

        // 파라미터 유효성 검증
        if (pointsToUse == null) {
            throw new IllegalArgumentException("사용할 포인트를 입력해주세요.");
        }
        if (pointsToUse <= 0) {
            throw new IllegalArgumentException("포인트는 양수여야 합니다.");
        }
        if (rewardType == null || rewardType.trim().isEmpty()) {
            throw new IllegalArgumentException("상품권 타입을 입력해주세요.");
        }

        // 포인트 부족 검증
        if (!user.hasEnoughPoints(pointsToUse)) {
            throw new IllegalStateException("보유 포인트가 부족합니다. 현재: " + user.getPoints() + ", 필요: " + pointsToUse);
        }

        // 포인트 차감
        user.usePoints(pointsToUse);
        
        // 상품권 교환 정보 생성 (즉시 승인 상태)
        Reward reward = Reward.builder()
                .user(user)
                .pointsUsed(pointsToUse)
                .rewardType(rewardType)
                .status(Reward.RewardStatus.APPROVED) // 즉시 승인
                .build();
        
        // 승인 시간 설정
        reward.approve();
        
        Reward savedReward = rewardRepository.save(reward);
        
        // User 엔티티에도 교환 요청 추가
        user.addReward(savedReward);
        
        // 핀번호 즉시 발급
        try {
            rewardPinService.issuePin(savedReward);
            log.info("상품권 교환 및 핀번호 발급 완료: ID={}", savedReward.getId());
        } catch (Exception e) {
            log.error("핀번호 발급 실패: rewardId={}", savedReward.getId(), e);
            // 핀번호 발급 실패 시 상품권 교환도 취소
            user.addPoints(pointsToUse); // 포인트 복구
            rewardRepository.delete(savedReward);
            throw new RuntimeException("상품권 교환 중 오류가 발생했습니다. 다시 시도해주세요.", e);
        }
        
        // 포인트 사용 이력 생성
        pointHistoryService.createUsedHistory(user, pointsToUse, 
                "상품권 교환 완료: " + rewardType);
        
        log.info("상품권 즉시 교환 완료: ID={}", savedReward.getId());
        return savedReward;
    }



    /**
     * 특정 사용자의 상품권 교환 요청을 최신순으로 조회합니다.
     * 
     * @param user 조회할 사용자
     * @return 상품권 교환 요청 목록 (최신순)
     */
    public List<Reward> getRewardsByUser(User user) {
        log.debug("사용자 상품권 교환 요청 조회: 사용자={}", user.getUsername());
        return rewardRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * 특정 상태의 상품권 교환 요청을 최신순으로 조회합니다.
     * 
     * @param status 상품권 교환 상태
     * @return 해당 상태의 상품권 교환 요청 목록
     */
    public List<Reward> getRewardsByStatus(Reward.RewardStatus status) {
        log.debug("상품권 교환 요청 상태별 조회: 상태={}", status);
        return rewardRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    /**
     * 특정 사용자의 특정 상태 상품권 교환 요청을 조회합니다.
     * 
     * @param user 조회할 사용자
     * @param status 상품권 교환 상태
     * @return 해당 조건의 상품권 교환 요청 목록
     */
    public List<Reward> getRewardsByUserAndStatus(User user, Reward.RewardStatus status) {
        log.debug("사용자 및 상태별 상품권 교환 요청 조회: 사용자={}, 상태={}", user.getUsername(), status);
        return rewardRepository.findByUserAndStatusOrderByCreatedAtDesc(user, status);
    }

    /**
     * 특정 기간 동안의 상품권 교환 요청을 조회합니다.
     * 
     * @param startDate 시작일시
     * @param endDate 종료일시
     * @return 해당 기간의 상품권 교환 요청 목록
     */
    public List<Reward> getRewardsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("상품권 교환 요청 기간별 조회: 시작={}, 종료={}", startDate, endDate);
        return rewardRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
    }

    /**
     * 특정 사용자의 대기중인 상품권 교환 요청을 조회합니다.
     * 
     * @param user 조회할 사용자
     * @return 대기중인 상품권 교환 요청 목록
     */
    public List<Reward> getPendingRewardsByUser(User user) {
        log.debug("사용자 대기중 상품권 교환 요청 조회: 사용자={}", user.getUsername());
        return rewardRepository.findPendingRewardsByUser(user);
    }



    /**
     * 특정 사용자의 총 사용 포인트를 계산합니다 (승인된 요청만).
     * 
     * @param user 조회할 사용자
     * @return 총 사용 포인트
     */
    public Integer getTotalUsedPointsByUser(User user) {
        log.debug("사용자 총 사용 포인트 조회: 사용자={}", user.getUsername());
        return rewardRepository.getTotalUsedPointsByUser(user);
    }

    /**
     * 특정 상품권 타입별 교환 횟수를 조회합니다.
     * 
     * @param rewardType 상품권 타입
     * @param status 교환 상태
     * @return 해당 타입의 교환 횟수
     */
    public Long getRewardCountByType(String rewardType, Reward.RewardStatus status) {
        log.debug("상품권 타입별 교환 횟수 조회: 타입={}, 상태={}", rewardType, status);
        return rewardRepository.countByRewardTypeAndStatus(rewardType, status);
    }

    /**
     * 특정 사용자의 월별 상품권 교환 통계를 조회합니다.
     * 
     * @param user 조회할 사용자
     * @param year 조회할 연도
     * @param month 조회할 월
     * @return 해당 월의 총 사용 포인트
     */
    public Integer getMonthlyUsedPoints(User user, int year, int month) {
        log.debug("사용자 월별 사용 포인트 조회: 사용자={}, 연도={}, 월={}", user.getUsername(), year, month);
        return rewardRepository.getMonthlyUsedPoints(user, year, month);
    }

    /**
     * 상품권 교환 가능 여부를 확인합니다.
     * 
     * @param user 확인할 사용자
     * @param pointsToUse 사용할 포인트 수량
     * @return 교환 가능하면 true
     */
    public boolean canExchangeReward(User user, Integer pointsToUse) {
        return user.hasEnoughPoints(pointsToUse);
    }

    /**
     * 상품권 교환 요청 상세 정보를 조회합니다.
     * 
     * @param rewardId 교환 요청 ID
     * @return 교환 요청 상세 정보
     * @throws IllegalArgumentException 교환 요청을 찾을 수 없는 경우
     */
    public Reward getRewardById(Long rewardId) {
        log.debug("상품권 교환 요청 상세 조회: ID={}", rewardId);
        return rewardRepository.findById(rewardId)
                .orElseThrow(() -> new IllegalArgumentException("상품권 교환 요청을 찾을 수 없습니다: " + rewardId));
    }
}
