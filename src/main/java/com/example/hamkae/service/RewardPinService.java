package com.example.hamkae.service;

import com.example.hamkae.domain.Reward;
import com.example.hamkae.domain.RewardPin;
import com.example.hamkae.domain.User;
import com.example.hamkae.repository.RewardPinRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 상품권 핀번호 관련 비즈니스 로직을 처리하는 서비스 클래스
 * 핀번호 발급, 사용, 조회 등의 기능을 제공합니다.
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-01-14
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RewardPinService {

    private final RewardPinRepository rewardPinRepository;

    /**
     * 상품권 교환 승인 시 핀번호를 발급합니다.
     * 
     * @param reward 승인된 상품권 교환 요청
     * @return 발급된 핀번호
     * @throws IllegalStateException 이미 핀번호가 발급된 경우
     */
    @Transactional
    public RewardPin issuePin(Reward reward) {
        log.info("핀번호 발급 시작: rewardId={}, 사용자={}, 상품권타입={}", 
                reward.getId(), reward.getUser().getUsername(), reward.getRewardType());

        // 이미 핀번호가 발급되었는지 확인
        if (rewardPinRepository.existsByReward(reward)) {
            throw new IllegalStateException("이미 핀번호가 발급된 상품권 교환 요청입니다: " + reward.getId());
        }

        // 승인된 상품권만 핀번호 발급 가능
        if (!reward.isApproved()) {
            throw new IllegalStateException("승인되지 않은 상품권 교환 요청입니다: " + reward.getId());
        }

        // 핀번호 생성 (중복 방지)
        RewardPin rewardPin = generateUniquePin(reward);
        RewardPin savedPin = rewardPinRepository.save(rewardPin);

        log.info("핀번호 발급 완료: rewardId={}, pinId={}, 마스킹핀={}", 
                reward.getId(), savedPin.getId(), savedPin.getMaskedPinNumber());

        return savedPin;
    }

    /**
     * 중복되지 않는 고유한 핀번호를 생성합니다.
     * 
     * @param reward 상품권 교환 요청
     * @return 생성된 RewardPin 객체
     */
    private RewardPin generateUniquePin(Reward reward) {
        int maxAttempts = 10; // 최대 10번 시도
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            RewardPin pin = RewardPin.generatePin(reward);
            
            // 핀번호 중복 확인
            if (!rewardPinRepository.existsByPinNumber(pin.getPinNumber())) {
                log.debug("고유 핀번호 생성 성공: 시도={}, 마스킹핀={}", attempt, pin.getMaskedPinNumber());
                return pin;
            }
            
            log.warn("핀번호 중복 발생, 재시도: 시도={}", attempt);
        }
        
        throw new RuntimeException("고유한 핀번호 생성에 실패했습니다. 최대 시도 횟수 초과: " + maxAttempts);
    }

    /**
     * 핀번호를 사용 처리합니다.
     * 
     * @param pinNumber 사용할 핀번호
     * @return 사용 처리된 핀번호
     * @throws IllegalArgumentException 핀번호를 찾을 수 없는 경우
     * @throws IllegalStateException 이미 사용되었거나 만료된 경우
     */
    @Transactional
    public RewardPin usePin(String pinNumber) {
        log.info("핀번호 사용 처리 시작: 마스킹핀={}", maskPinNumber(pinNumber));

        RewardPin pin = rewardPinRepository.findByPinNumber(pinNumber)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 핀번호입니다"));

        if (pin.getIsUsed()) {
            throw new IllegalStateException("이미 사용된 핀번호입니다");
        }

        if (pin.isExpired()) {
            throw new IllegalStateException("만료된 핀번호입니다");
        }

        pin.markAsUsed();
        RewardPin usedPin = rewardPinRepository.save(pin);

        log.info("핀번호 사용 처리 완료: 사용자={}, 상품권타입={}", 
                pin.getUser().getUsername(), pin.getRewardType());

        return usedPin;
    }

    /**
     * 특정 사용자의 모든 핀번호를 조회합니다.
     * 
     * @param user 조회할 사용자
     * @return 핀번호 목록 (최신순)
     */
    public List<RewardPin> getPinsByUser(User user) {
        log.debug("사용자 핀번호 목록 조회: 사용자={}", user.getUsername());
        return rewardPinRepository.findByUserOrderByIssuedAtDesc(user);
    }

    /**
     * 특정 사용자의 사용 가능한 핀번호를 조회합니다.
     * 
     * @param user 조회할 사용자
     * @return 사용 가능한 핀번호 목록
     */
    public List<RewardPin> getAvailablePinsByUser(User user) {
        log.debug("사용자 사용 가능 핀번호 조회: 사용자={}", user.getUsername());
        return rewardPinRepository.findAvailablePinsByUser(user, LocalDateTime.now());
    }

    /**
     * 특정 사용자의 사용된 핀번호를 조회합니다.
     * 
     * @param user 조회할 사용자
     * @return 사용된 핀번호 목록
     */
    public List<RewardPin> getUsedPinsByUser(User user) {
        log.debug("사용자 사용된 핀번호 조회: 사용자={}", user.getUsername());
        return rewardPinRepository.findUsedPinsByUser(user);
    }

    /**
     * 상품권 교환 요청의 핀번호를 조회합니다.
     * 
     * @param reward 조회할 상품권 교환 요청
     * @return 핀번호 정보 (Optional)
     */
    public Optional<RewardPin> getPinByReward(Reward reward) {
        log.debug("상품권 교환 요청 핀번호 조회: rewardId={}", reward.getId());
        return rewardPinRepository.findByReward(reward);
    }

    /**
     * 상품권 교환 요청 ID로 핀번호를 조회합니다.
     * 
     * @param rewardId 상품권 교환 요청 ID
     * @return 핀번호 정보 (Optional)
     */
    public Optional<RewardPin> getPinByRewardId(Long rewardId) {
        log.debug("상품권 교환 요청 ID로 핀번호 조회: rewardId={}", rewardId);
        return rewardPinRepository.findByRewardId(rewardId);
    }

    /**
     * 핀번호로 상품권 정보를 조회합니다.
     * 
     * @param pinNumber 조회할 핀번호
     * @return 핀번호 정보 (Optional)
     */
    public Optional<RewardPin> getPinByPinNumber(String pinNumber) {
        log.debug("핀번호로 상품권 정보 조회: 마스킹핀={}", maskPinNumber(pinNumber));
        return rewardPinRepository.findByPinNumber(pinNumber);
    }

    /**
     * 만료된 핀번호를 조회합니다.
     * 
     * @return 만료된 핀번호 목록
     */
    public List<RewardPin> getExpiredPins() {
        log.debug("만료된 핀번호 조회");
        return rewardPinRepository.findExpiredPins(LocalDateTime.now());
    }

    /**
     * 특정 기간에 발급된 핀번호를 조회합니다.
     * 
     * @param startDate 시작일시
     * @param endDate 종료일시
     * @return 해당 기간에 발급된 핀번호 목록
     */
    public List<RewardPin> getPinsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("기간별 핀번호 조회: 시작={}, 종료={}", startDate, endDate);
        return rewardPinRepository.findByIssuedAtBetweenOrderByIssuedAtDesc(startDate, endDate);
    }

    /**
     * 특정 상품권 타입의 발급 통계를 조회합니다.
     * 
     * @param rewardType 상품권 타입
     * @return 발급 수
     */
    public Long getPinCountByRewardType(String rewardType) {
        log.debug("상품권 타입별 발급 통계: 타입={}", rewardType);
        return rewardPinRepository.countByRewardType(rewardType);
    }

    /**
     * 특정 사용자의 특정 상품권 타입 핀번호를 조회합니다.
     * 
     * @param user 조회할 사용자
     * @param rewardType 상품권 타입
     * @return 해당 조건의 핀번호 목록
     */
    public List<RewardPin> getPinsByUserAndRewardType(User user, String rewardType) {
        log.debug("사용자 및 상품권 타입별 핀번호 조회: 사용자={}, 타입={}", user.getUsername(), rewardType);
        return rewardPinRepository.findByUserAndRewardType(user, rewardType);
    }

    /**
     * 특정 사용자의 총 핀번호 발급 수를 조회합니다.
     * 
     * @param user 조회할 사용자
     * @return 총 발급 수
     */
    public Long getPinCountByUser(User user) {
        log.debug("사용자 총 핀번호 발급 수 조회: 사용자={}", user.getUsername());
        return rewardPinRepository.countByUser(user);
    }

    /**
     * 핀번호가 이미 발급되었는지 확인합니다.
     * 
     * @param reward 확인할 상품권 교환 요청
     * @return 발급되었으면 true
     */
    public boolean isPinIssued(Reward reward) {
        return rewardPinRepository.existsByReward(reward);
    }

    /**
     * 보안을 위해 핀번호를 마스킹합니다.
     * 
     * @param pinNumber 원본 핀번호
     * @return 마스킹된 핀번호
     */
    private String maskPinNumber(String pinNumber) {
        if (pinNumber == null || pinNumber.length() < 19) {
            return "****-****-****-****";
        }
        
        String lastSegment = pinNumber.substring(15); // 마지막 4자리
        return "****-****-****-" + lastSegment;
    }
}
