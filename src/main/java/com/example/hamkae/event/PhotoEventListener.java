package com.example.hamkae.event;

import com.example.hamkae.service.AiVerificationTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

/**
 * 사진 업로드 이벤트 리스너
 * 트랜잭션이 완료된 후 AI 검증을 비동기로 수행합니다.
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-20
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PhotoEventListener {

    private final AiVerificationTaskService aiVerificationTaskService;

    /**
     * AFTER 사진 업로드 완료 후 AI 검증을 수행합니다.
     * 트랜잭션 커밋 후에 실행되어 데이터 일관성을 보장합니다.
     * 
     * @param event 사진 업로드 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePhotoUploaded(PhotoUploadedEvent event) {
        if ("AFTER".equals(event.getPhotoType())) {
            log.info("AFTER 사진 업로드 이벤트 수신: markerId={}, userId={}", 
                    event.getMarkerId(), event.getUserId());
            
            try {
                // 트랜잭션이 완료된 후 AI 검증 수행
                aiVerificationTaskService.verifyMarkerAsync(event.getMarkerId(), event.getUserId());
            } catch (Exception e) {
                log.error("AI 검증 이벤트 처리 중 오류: markerId={}, userId={}", 
                        event.getMarkerId(), event.getUserId(), e);
            }
        }
    }
}
