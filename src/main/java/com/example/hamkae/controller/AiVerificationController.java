package com.example.hamkae.controller;

import com.example.hamkae.DTO.ApiResponse;
import com.example.hamkae.DTO.GptVerificationResponseDTO;
import com.example.hamkae.domain.Photo;
import com.example.hamkae.repository.PhotoRepository;
import com.example.hamkae.service.GptVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI 검증 시스템을 위한 컨트롤러
 * GPT API를 통한 사진 비교 검증 기능을 제공합니다.
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-15
 */
@Slf4j
@RestController
@RequestMapping("/ai-verification")
@RequiredArgsConstructor
@Tag(name = "AI 검증", description = "GPT API를 통한 사진 비교 검증 API")
public class AiVerificationController {

    private final GptVerificationService gptVerificationService;
    private final PhotoRepository photoRepository;

    /**
     * 수동으로 AI 검증을 수행합니다.
     * 테스트 및 디버깅 목적으로 사용됩니다.
     * 
     * @param markerId 마커 ID
     * @return 검증 결과
     */
    @PostMapping("/verify/{markerId}")
    @Operation(
        summary = "수동 AI 검증",
        description = "특정 마커의 BEFORE/AFTER 사진을 비교하여 AI 검증을 수행합니다."
    )
    public ResponseEntity<ApiResponse<GptVerificationResponseDTO>> manualVerification(
            @Parameter(description = "검증할 마커 ID", required = true)
            @PathVariable Long markerId) {
        
        try {
            log.info("수동 AI 검증 요청: markerId={}", markerId);
            
            // 마커의 BEFORE 사진 조회
            var beforePhotos = photoRepository.findByMarkerIdAndType(markerId, Photo.PhotoType.BEFORE);
            if (beforePhotos.isEmpty()) {
                            return ResponseEntity.badRequest().body(
                ApiResponse.error("BEFORE 사진이 없습니다. 마커를 먼저 등록해주세요.")
            );
            }
            
            // 마커의 AFTER 사진 조회
            var afterPhotos = photoRepository.findByMarkerIdAndType(markerId, Photo.PhotoType.AFTER);
            if (afterPhotos.isEmpty()) {
                            return ResponseEntity.badRequest().body(
                ApiResponse.error("AFTER 사진이 없습니다. 청소 인증 사진을 먼저 업로드해주세요.")
            );
            }
            
            // 첫 번째 사진들로 검증 수행
            Photo beforePhoto = beforePhotos.get(0);
            Photo afterPhoto = afterPhotos.get(0);
            
            GptVerificationResponseDTO result = gptVerificationService.verifyCleanup(beforePhoto, afterPhoto);
            
            // 검증 결과를 사진에 저장
            if (result.isSuccess()) {
                if ("APPROVED".equals(result.getVerificationResult())) {
                    afterPhoto.approve(result.getGptResponse());
                } else {
                    afterPhoto.reject(result.getGptResponse());
                }
                photoRepository.save(afterPhoto);
            }
            
            log.info("수동 AI 검증 완료: markerId={}, 결과={}", markerId, result.getVerificationResult());
            
            return ResponseEntity.ok(ApiResponse.success("AI 검증이 완료되었습니다.", result));
            
        } catch (Exception e) {
            log.error("수동 AI 검증 중 오류 발생: markerId={}", markerId, e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("AI 검증 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }

    /**
     * AI 검증 상태를 조회합니다.
     * 
     * @param markerId 마커 ID
     * @return 검증 상태 정보
     */
    @GetMapping("/status/{markerId}")
    @Operation(
        summary = "AI 검증 상태 조회",
        description = "특정 마커의 AI 검증 상태를 조회합니다."
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> getVerificationStatus(
            @Parameter(description = "마커 ID", required = true)
            @PathVariable Long markerId) {
        
        try {
            var beforePhotos = photoRepository.findByMarkerIdAndType(markerId, Photo.PhotoType.BEFORE);
            var afterPhotos = photoRepository.findByMarkerIdAndType(markerId, Photo.PhotoType.AFTER);
            
            Map<String, Object> status = Map.of(
                "markerId", markerId,
                "hasBeforePhotos", !beforePhotos.isEmpty(),
                "beforePhotoCount", beforePhotos.size(),
                "hasAfterPhotos", !afterPhotos.isEmpty(),
                "afterPhotoCount", afterPhotos.size(),
                "verificationStatus", afterPhotos.isEmpty() ? "PENDING" : 
                    afterPhotos.get(0).getVerificationStatus().name(),
                "gptResponse", afterPhotos.isEmpty() ? null : 
                    afterPhotos.get(0).getGptResponse()
            );
            
            return ResponseEntity.ok(ApiResponse.success("검증 상태 조회 완료", status));
            
        } catch (Exception e) {
            log.error("검증 상태 조회 중 오류 발생: markerId={}", markerId, e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("검증 상태 조회 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }

    /**
     * AI 검증 시스템 상태를 확인합니다.
     * 
     * @return 시스템 상태 정보
     */
    @GetMapping("/health")
    @Operation(
        summary = "AI 검증 시스템 상태 확인",
        description = "AI 검증 시스템의 상태를 확인합니다."
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemHealth() {
        try {
            Map<String, Object> health = Map.of(
                "status", "HEALTHY",
                "service", "AI Verification System",
                "timestamp", System.currentTimeMillis(),
                "pointsReward", gptVerificationService.getPointsReward()
            );
            
            return ResponseEntity.ok(ApiResponse.success("시스템 상태 확인 완료", health));
            
        } catch (Exception e) {
            log.error("시스템 상태 확인 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("시스템 상태 확인 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }
}
