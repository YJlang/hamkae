package com.example.hamkae.controller;

import com.example.hamkae.DTO.ApiResponse;
import com.example.hamkae.DTO.PointStatisticsResponseDTO;
import com.example.hamkae.DTO.UserProfileResponseDTO;
import com.example.hamkae.config.JwtUtil;
import com.example.hamkae.domain.User;
import com.example.hamkae.service.PointHistoryService;
import com.example.hamkae.service.RewardPinService;
import com.example.hamkae.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 사용자 프로필 관련 API를 제공하는 컨트롤러 클래스
 * 사용자 프로필 조회, 포인트 현황 등의 기능을 제공합니다.
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-01-14
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "사용자 프로필", description = "사용자 프로필 및 현황 관련 API")
public class UserController {

    private final UserService userService;
    private final PointHistoryService pointHistoryService;
    private final RewardPinService rewardPinService;
    private final JwtUtil jwtUtil;

    /**
     * 현재 사용자의 프로필 정보를 조회합니다.
     * 
     * @param authorization JWT 토큰
     * @return 사용자 프로필 정보
     */
    @GetMapping("/profile")
    @Operation(summary = "사용자 프로필 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> getProfile(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String authorization) {
        
        String username = null;
        try {
            String token = authorization.replace("Bearer ", "");
            username = jwtUtil.validateAndGetUsername(token);
            User user = userService.findByUsername(username);
            
            // 포인트 통계 조회
            Integer totalEarnedPoints = pointHistoryService.getTotalEarnedPoints(user);
            Integer totalUsedPoints = pointHistoryService.getTotalUsedPoints(user);
            Long issuedPinsCount = rewardPinService.getPinCountByUser(user);
            
            UserProfileResponseDTO response = UserProfileResponseDTO.withPointStatistics(
                    user, totalEarnedPoints, totalUsedPoints, issuedPinsCount.intValue());
            
            return ResponseEntity.ok(ApiResponse.success("사용자 프로필 조회 성공", response));
            
        } catch (Exception e) {
            log.error("사용자 프로필 조회 실패: username={}, error={}", username, e.getMessage(), e);
            
            String errorMessage;
            if (e.getMessage() != null && e.getMessage().contains("사용자를 찾을 수 없습니다")) {
                errorMessage = "사용자 정보를 찾을 수 없습니다. 다시 로그인해주세요.";
            } else if (e.getMessage() != null && e.getMessage().contains("토큰")) {
                errorMessage = "인증이 만료되었습니다. 다시 로그인해주세요.";
            } else {
                errorMessage = "사용자 정보를 불러오는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
            }
            
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
        }
    }

    /**
     * 현재 사용자의 포인트 현황을 조회합니다.
     * 
     * @param authorization JWT 토큰
     * @return 포인트 통계 정보
     */
    @GetMapping("/points/summary")
    @Operation(summary = "포인트 현황 조회", description = "현재 사용자의 포인트 적립/사용 현황을 조회합니다.")
    public ResponseEntity<ApiResponse<PointStatisticsResponseDTO>> getPointsSummary(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String authorization) {
        
        try {
            String token = authorization.replace("Bearer ", "");
            String username = jwtUtil.validateAndGetUsername(token);
            User user = userService.findByUsername(username);
            
            PointHistoryService.PointStatistics statistics = pointHistoryService.getPointStatistics(user);
            PointStatisticsResponseDTO response = PointStatisticsResponseDTO.from(statistics);
            
            return ResponseEntity.ok(ApiResponse.success("포인트 현황 조회 성공", response));
            
        } catch (Exception e) {
            log.error("포인트 현황 조회 실패: error={}", e.getMessage(), e);
            
            String errorMessage;
            if (e.getMessage() != null && e.getMessage().contains("사용자를 찾을 수 없습니다")) {
                errorMessage = "사용자 정보를 찾을 수 없습니다. 다시 로그인해주세요.";
            } else if (e.getMessage() != null && e.getMessage().contains("토큰")) {
                errorMessage = "인증이 만료되었습니다. 다시 로그인해주세요.";
            } else {
                errorMessage = "포인트 현황을 불러오는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
            }
            
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
        }
    }

    /**
     * 현재 사용자의 활동 요약을 조회합니다.
     * 
     * @param authorization JWT 토큰
     * @return 활동 요약 정보
     */
    @GetMapping("/activity/summary")
    @Operation(summary = "활동 요약 조회", description = "현재 사용자의 쓰레기 제보, 청소 인증 등 활동 요약을 조회합니다.")
    public ResponseEntity<ApiResponse<UserActivitySummaryDTO>> getActivitySummary(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String authorization) {
        
        try {
            String token = authorization.replace("Bearer ", "");
            String username = jwtUtil.validateAndGetUsername(token);
            User user = userService.findByUsername(username);
            
            // 활동 요약 정보 구성
            UserActivitySummaryDTO summary = UserActivitySummaryDTO.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .name(user.getName())
                    .currentPoints(user.getPoints())
                    .totalEarnedPoints(pointHistoryService.getTotalEarnedPoints(user))
                    .totalUsedPoints(pointHistoryService.getTotalUsedPoints(user))
                    .reportedMarkersCount(user.getReportedMarkers() != null ? user.getReportedMarkers().size() : 0)
                    .uploadedPhotosCount(user.getUploadedPhotos() != null ? user.getUploadedPhotos().size() : 0)
                    .rewardExchangeCount(user.getRewards() != null ? user.getRewards().size() : 0)
                    .issuedPinsCount(rewardPinService.getPinCountByUser(user).intValue())
                    .memberSince(user.getCreatedAt())
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success("활동 요약 조회 성공", summary));
            
        } catch (Exception e) {
            log.error("활동 요약 조회 실패: error={}", e.getMessage(), e);
            
            String errorMessage;
            if (e.getMessage() != null && e.getMessage().contains("사용자를 찾을 수 없습니다")) {
                errorMessage = "사용자 정보를 찾을 수 없습니다. 다시 로그인해주세요.";
            } else if (e.getMessage() != null && e.getMessage().contains("토큰")) {
                errorMessage = "인증이 만료되었습니다. 다시 로그인해주세요.";
            } else {
                errorMessage = "활동 요약을 불러오는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
            }
            
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
        }
    }

    /**
     * 테스트용 포인트 조작 API (개발/테스트 환경에서만 사용)
     * JSON Body 방식과 URL 파라미터 방식 모두 지원
     * 
     * @param authorization JWT 토큰
     * @param requestDTO 포인트 조작 요청 정보 (JSON Body)
     * @param pointsParam URL 파라미터로 전달된 포인트 값
     * @return 업데이트된 사용자 정보
     */
    @PostMapping(value = "/points/admin-set", consumes = {"application/json", "text/plain", "*/*"})
    @Operation(summary = "포인트 강제 설정 (테스트용)", 
               description = "개발/테스트를 위한 포인트 강제 설정 API. JSON Body 또는 URL 파라미터로 사용 가능. 운영 환경에서는 사용하지 마세요.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> setPointsForTesting(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "포인트 설정 요청 (JSON Body)", required = false)
            @RequestBody(required = false) Map<String, Integer> requestDTO,
            @Parameter(description = "포인트 값 (URL 파라미터)", required = false)
            @RequestParam(value = "points", required = false) Integer pointsParam) {
        
        try {
            String token = authorization.replace("Bearer ", "");
            String username = jwtUtil.validateAndGetUsername(token);
            User user = userService.findByUsername(username);
            
            // JSON Body 또는 URL 파라미터에서 포인트 값 가져오기
            Integer newPoints = null;
            
            // 1. JSON Body에서 먼저 확인
            if (requestDTO != null && requestDTO.containsKey("points")) {
                newPoints = requestDTO.get("points");
                log.debug("포인트 값을 JSON Body에서 가져옴: {}", newPoints);
            }
            // 2. URL 파라미터에서 확인
            else if (pointsParam != null) {
                newPoints = pointsParam;
                log.debug("포인트 값을 URL 파라미터에서 가져옴: {}", newPoints);
            }
            
            if (newPoints == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("포인트 값을 입력해주세요. JSON Body: {\"points\": 10000} 또는 URL 파라미터: ?points=10000"));
            }
            
            if (newPoints < 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("포인트는 0 이상이어야 합니다."));
            }
            
            Integer oldPoints = user.getPoints();
            user.setPoints(newPoints);
            userService.save(user);
            
            log.warn("테스트용 포인트 강제 설정: 사용자={}, 기존={}pt, 신규={}pt", 
                    username, oldPoints, newPoints);
            
            Map<String, Object> result = new HashMap<>();
            result.put("username", username);
            result.put("oldPoints", oldPoints);
            result.put("newPoints", newPoints);
            result.put("message", "포인트가 성공적으로 설정되었습니다.");
            
            return ResponseEntity.ok(ApiResponse.success("포인트 설정 완료", result));
            
        } catch (Exception e) {
            log.error("포인트 설정 실패: error={}", e.getMessage(), e);
            
            String errorMessage;
            if (e.getMessage() != null && e.getMessage().contains("사용자를 찾을 수 없습니다")) {
                errorMessage = "사용자 정보를 찾을 수 없습니다. 다시 로그인해주세요.";
            } else if (e.getMessage() != null && e.getMessage().contains("토큰")) {
                errorMessage = "인증이 만료되었습니다. 다시 로그인해주세요.";
            } else {
                errorMessage = "포인트 설정에 실패했습니다. 잠시 후 다시 시도해주세요.";
            }
            
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
        }
    }

    /**
     * 테스트용 포인트 조작 API (GET 방식, 더 간단한 사용)
     * 
     * @param authorization JWT 토큰
     * @param points 설정할 포인트 값
     * @return 업데이트된 사용자 정보
     */
    @GetMapping("/points/admin-set")
    @Operation(summary = "포인트 강제 설정 (GET 방식)", 
               description = "GET 방식으로 포인트를 설정합니다. 예: /api/users/points/admin-set?points=10000")
    public ResponseEntity<ApiResponse<Map<String, Object>>> setPointsWithGet(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "설정할 포인트 값", required = true)
            @RequestParam("points") Integer points) {
        
        try {
            String token = authorization.replace("Bearer ", "");
            String username = jwtUtil.validateAndGetUsername(token);
            User user = userService.findByUsername(username);
            
            if (points < 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("포인트는 0 이상이어야 합니다."));
            }
            
            Integer oldPoints = user.getPoints();
            user.setPoints(points);
            userService.save(user);
            
            log.warn("테스트용 포인트 강제 설정 (GET): 사용자={}, 기존={}pt, 신규={}pt", 
                    username, oldPoints, points);
            
            Map<String, Object> result = new HashMap<>();
            result.put("username", username);
            result.put("oldPoints", oldPoints);
            result.put("newPoints", points);
            result.put("method", "GET");
            result.put("message", "포인트가 성공적으로 설정되었습니다.");
            
            return ResponseEntity.ok(ApiResponse.success("포인트 설정 완료 (GET 방식)", result));
            
        } catch (Exception e) {
            log.error("포인트 설정 실패 (GET): error={}", e.getMessage(), e);
            
            String errorMessage;
            if (e.getMessage() != null && e.getMessage().contains("사용자를 찾을 수 없습니다")) {
                errorMessage = "사용자 정보를 찾을 수 없습니다. 다시 로그인해주세요.";
            } else if (e.getMessage() != null && e.getMessage().contains("토큰")) {
                errorMessage = "인증이 만료되었습니다. 다시 로그인해주세요.";
            } else {
                errorMessage = "포인트 설정에 실패했습니다. 잠시 후 다시 시도해주세요.";
            }
            
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
        }
    }

    /**
     * 사용자 활동 요약을 위한 내부 DTO 클래스
     */
    @lombok.Builder
    @lombok.Data
    public static class UserActivitySummaryDTO {
        private Long userId;
        private String username;
        private String name;
        private Integer currentPoints;
        private Integer totalEarnedPoints;
        private Integer totalUsedPoints;
        private Integer reportedMarkersCount;
        private Integer uploadedPhotosCount;
        private Integer rewardExchangeCount;
        private Integer issuedPinsCount;
        private java.time.LocalDateTime memberSince;
    }
}
