package com.example.hamkae.service;

import com.example.hamkae.domain.PointHistory;
import com.example.hamkae.domain.Photo;
import com.example.hamkae.domain.User;
import com.example.hamkae.repository.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 포인트 이력 관련 비즈니스 로직을 처리하는 서비스 클래스
 * 포인트 적립, 사용, 조회 등의 기능을 제공합니다.
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-20
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PointHistoryService {

    private final PointHistoryRepository pointHistoryRepository;

    /**
     * 포인트 적립 이력을 생성하고 저장합니다.
     * 
     * @param user 포인트를 적립할 사용자
     * @param points 적립할 포인트 수량
     * @param description 적립 사유
     * @param relatedPhoto 관련 사진 (선택사항)
     * @return 생성된 포인트 이력
     */
    @Transactional
    public PointHistory createEarnedHistory(User user, Integer points, String description, Photo relatedPhoto) {
        log.info("포인트 적립 이력 생성: 사용자={}, 포인트={}, 사유={}", user.getUsername(), points, description);
        
        PointHistory history = PointHistory.createEarnedHistory(user, points, description, relatedPhoto);
        PointHistory savedHistory = pointHistoryRepository.save(history);
        
        // User 엔티티에도 이력 추가
        user.addPointHistory(savedHistory);
        
        log.info("포인트 적립 이력 생성 완료: ID={}", savedHistory.getId());
        return savedHistory;
    }

    /**
     * 포인트 사용 이력을 생성하고 저장합니다.
     * 
     * @param user 포인트를 사용할 사용자
     * @param points 사용할 포인트 수량
     * @param description 사용 사유
     * @return 생성된 포인트 이력
     */
    @Transactional
    public PointHistory createUsedHistory(User user, Integer points, String description) {
        log.info("포인트 사용 이력 생성: 사용자={}, 포인트={}, 사유={}", user.getUsername(), points, description);
        
        PointHistory history = PointHistory.createUsedHistory(user, points, description);
        PointHistory savedHistory = pointHistoryRepository.save(history);
        
        // User 엔티티에도 이력 추가
        user.addPointHistory(savedHistory);
        
        log.info("포인트 사용 이력 생성 완료: ID={}", savedHistory.getId());
        return savedHistory;
    }

    /**
     * 특정 사용자의 포인트 이력을 최신순으로 조회합니다.
     * 
     * @param user 조회할 사용자
     * @return 포인트 이력 목록 (최신순)
     */
    public List<PointHistory> getPointHistoriesByUser(User user) {
        log.debug("사용자 포인트 이력 조회: 사용자={}", user.getUsername());
        return pointHistoryRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * 특정 사용자의 포인트 이력을 타입별로 조회합니다.
     * 
     * @param user 조회할 사용자
     * @param type 포인트 타입 (EARNED/USED)
     * @return 해당 타입의 포인트 이력 목록
     */
    public List<PointHistory> getPointHistoriesByUserAndType(User user, PointHistory.PointType type) {
        log.debug("사용자 포인트 이력 타입별 조회: 사용자={}, 타입={}", user.getUsername(), type);
        return pointHistoryRepository.findByUserAndTypeOrderByCreatedAtDesc(user, type);
    }

    /**
     * 특정 기간 동안의 사용자 포인트 이력을 조회합니다.
     * 
     * @param user 조회할 사용자
     * @param startDate 시작일시
     * @param endDate 종료일시
     * @return 해당 기간의 포인트 이력 목록
     */
    public List<PointHistory> getPointHistoriesByUserAndDateRange(User user, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("사용자 포인트 이력 기간별 조회: 사용자={}, 시작={}, 종료={}", 
                 user.getUsername(), startDate, endDate);
        return pointHistoryRepository.findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(user, startDate, endDate);
    }

    /**
     * 특정 사용자의 총 적립 포인트를 계산합니다.
     * 
     * @param user 조회할 사용자
     * @return 총 적립 포인트
     */
    public Integer getTotalEarnedPoints(User user) {
        log.debug("사용자 총 적립 포인트 조회: 사용자={}", user.getUsername());
        return pointHistoryRepository.getTotalEarnedPointsByUser(user);
    }

    /**
     * 특정 사용자의 총 사용 포인트를 계산합니다.
     * 
     * @param user 조회할 사용자
     * @return 총 사용 포인트
     */
    public Integer getTotalUsedPoints(User user) {
        log.debug("사용자 총 사용 포인트 조회: 사용자={}", user.getUsername());
        return pointHistoryRepository.getTotalUsedPointsByUser(user);
    }

    /**
     * 특정 사용자의 월별 포인트 적립 통계를 조회합니다.
     * 
     * @param user 조회할 사용자
     * @param year 조회할 연도
     * @param month 조회할 월
     * @return 해당 월의 총 적립 포인트
     */
    public Integer getMonthlyEarnedPoints(User user, int year, int month) {
        log.debug("사용자 월별 적립 포인트 조회: 사용자={}, 연도={}, 월={}", user.getUsername(), year, month);
        return pointHistoryRepository.getMonthlyEarnedPoints(user, year, month);
    }

    /**
     * 특정 사용자의 최근 N개 포인트 이력을 조회합니다.
     * 
     * @param user 조회할 사용자
     * @param limit 조회할 개수
     * @return 최근 포인트 이력 목록
     */
    public List<PointHistory> getRecentPointHistories(User user, int limit) {
        log.debug("사용자 최근 포인트 이력 조회: 사용자={}, 개수={}", user.getUsername(), limit);
        return pointHistoryRepository.findRecentPointHistories(user, limit);
    }

    /**
     * 사용자의 포인트 적립과 사용 통계를 조회합니다.
     * 
     * @param user 조회할 사용자
     * @return 포인트 통계 정보를 담은 객체
     */
    public PointStatistics getPointStatistics(User user) {
        log.debug("사용자 포인트 통계 조회: 사용자={}", user.getUsername());
        
        Integer totalEarned = getTotalEarnedPoints(user);
        Integer totalUsed = getTotalUsedPoints(user);
        Integer currentPoints = user.getPoints();
        
        return PointStatistics.builder()
                .totalEarned(totalEarned)
                .totalUsed(totalUsed)
                .currentPoints(currentPoints)
                .availablePoints(currentPoints)
                .build();
    }

    /**
     * 포인트 통계 정보를 담는 내부 클래스
     */
    @lombok.Builder
    @lombok.Data
    public static class PointStatistics {
        private Integer totalEarned;    // 총 적립 포인트
        private Integer totalUsed;      // 총 사용 포인트
        private Integer currentPoints;  // 현재 보유 포인트
        private Integer availablePoints; // 사용 가능 포인트
    }
}
