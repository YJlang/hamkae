package com.example.hamkae.service;

import com.example.hamkae.DTO.GptVerificationResponseDTO;
import com.example.hamkae.domain.Photo;
import com.example.hamkae.domain.PointHistory;
import com.example.hamkae.domain.User;
import com.example.hamkae.repository.MarkerRepository;
import com.example.hamkae.repository.PhotoRepository;
import com.example.hamkae.repository.PointHistoryRepository;
import com.example.hamkae.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AI 검증을 백그라운드에서 실행하는 작업 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiVerificationTaskService {

    private final PhotoRepository photoRepository;
    private final MarkerRepository markerRepository;
    private final UserRepository userRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final GptVerificationService gptVerificationService;

    /**
     * 마커의 BEFORE/AFTER 사진을 비교하여 AI 검증을 비동기로 수행합니다.
     */
    @Async
    @Transactional
    public void verifyMarkerAsync(Long markerId, Long userId) {
        try {
            log.info("[ASYNC] AI 검증 시작: markerId={}, userId={}", markerId, userId);

            List<Photo> beforePhotos = photoRepository.findByMarkerIdAndType(markerId, Photo.PhotoType.BEFORE);
            if (beforePhotos.isEmpty()) {
                log.warn("[ASYNC] BEFORE 사진 없음: markerId={}", markerId);
                return;
            }

            List<Photo> afterPhotos = photoRepository.findByMarkerIdAndType(markerId, Photo.PhotoType.AFTER);
            if (afterPhotos.isEmpty()) {
                log.warn("[ASYNC] AFTER 사진 없음: markerId={}", markerId);
                return;
            }

            Photo beforePhoto = beforePhotos.get(0);
            Photo afterPhoto = afterPhotos.get(0);

            GptVerificationResponseDTO result = gptVerificationService.verifyCleanup(beforePhoto, afterPhoto);

            if (result.isSuccess()) {
                if ("APPROVED".equals(result.getVerificationResult())) {
                    afterPhoto.approve(result.getGptResponse());
                    // 포인트 적립 로직 실행 - userId로 다시 조회하여 세션 문제 해결
                    awardPointsForCleanupAsync(userId, afterPhoto.getId(), result);
                } else {
                    afterPhoto.reject(result.getGptResponse());
                }
            } else {
                afterPhoto.reject("AI 검증 실패: " + result.getErrorMessage());
            }

            photoRepository.save(afterPhoto);
            log.info("[ASYNC] AI 검증 완료: markerId={}, 결과={}", markerId, result.getVerificationResult());

        } catch (Exception e) {
            log.error("[ASYNC] AI 검증 중 오류: markerId={}, userId={}", markerId, userId, e);
        }
    }

    /**
     * AI 검증 승인 시 비동기로 사용자에게 포인트를 적립합니다.
     * 
     * @param userId 포인트를 적립받을 사용자 ID
     * @param photoId 검증된 사진 ID
     * @param verificationResult AI 검증 결과
     */
    private void awardPointsForCleanupAsync(Long userId, Long photoId, GptVerificationResponseDTO verificationResult) {
        try {
            // 세션 문제 해결을 위해 다시 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다: " + userId));
            Photo photo = photoRepository.findById(photoId)
                    .orElseThrow(() -> new IllegalStateException("사진을 찾을 수 없습니다: " + photoId));
            
            // 기본 포인트: 100pt
            Integer basePoints = 100;
            
            // 신뢰도에 따른 보너스 포인트 계산 (80% 이상 시 20pt 추가)
            Integer bonusPoints = 0;
            if (verificationResult.getConfidence() != null && verificationResult.getConfidence() >= 0.8) {
                bonusPoints = 20;
            }
            
            Integer totalPoints = basePoints + bonusPoints;
            
            log.info("[ASYNC] 포인트 적립 시작: 사용자={}, 기본={}pt, 보너스={}pt, 총={}pt", 
                    user.getUsername(), basePoints, bonusPoints, totalPoints);
            
            // 사용자 포인트 적립
            user.addPoints(totalPoints);
            userRepository.save(user);
            
            // 포인트 적립 이력 생성
            String description = String.format("청소 인증 완료 (신뢰도: %.0f%%)", 
                    verificationResult.getConfidence() != null ? verificationResult.getConfidence() * 100 : 0);
            
            PointHistory pointHistory = PointHistory.createEarnedHistory(user, totalPoints, description, photo);
            pointHistoryRepository.save(pointHistory);
            
            log.info("[ASYNC] 포인트 적립 완료: 사용자={}, 적립포인트={}pt, 현재보유={}pt", 
                    user.getUsername(), totalPoints, user.getPoints());
                    
        } catch (Exception e) {
            log.error("[ASYNC] 포인트 적립 중 오류 발생: userId={}, photoId={}", 
                    userId, photoId, e);
            // 포인트 적립 실패해도 AI 검증 결과는 유지
        }
    }
}


