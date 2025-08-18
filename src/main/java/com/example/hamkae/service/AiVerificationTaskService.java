package com.example.hamkae.service;

import com.example.hamkae.DTO.GptVerificationResponseDTO;
import com.example.hamkae.domain.Photo;
import com.example.hamkae.repository.MarkerRepository;
import com.example.hamkae.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
    private final GptVerificationService gptVerificationService;

    /**
     * 마커의 BEFORE/AFTER 사진을 비교하여 AI 검증을 비동기로 수행합니다.
     */
    @Async
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
}


