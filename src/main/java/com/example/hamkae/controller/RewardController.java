package com.example.hamkae.controller;

import com.example.hamkae.DTO.ApiResponse;
import com.example.hamkae.DTO.RewardRequestDTO;
import com.example.hamkae.DTO.RewardResponseDTO;

import com.example.hamkae.config.JwtUtil;
import com.example.hamkae.domain.Reward;
import com.example.hamkae.domain.User;
import com.example.hamkae.service.RewardService;
import com.example.hamkae.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 상품권 교환 관련 API를 제공하는 컨트롤러 클래스
 * 상품권 교환 요청, 승인, 거부, 조회 등의 기능을 제공합니다.
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-01-14
 */
@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "상품권 교환", description = "상품권 교환 관련 API")
public class RewardController {

    private final RewardService rewardService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 상품권을 즉시 교환합니다. (핀번호 포함)
     * 
     * @param authorization JWT 토큰
     * @param requestDTO 상품권 교환 요청 정보
     * @return 교환 완료된 상품권 정보 (핀번호 포함)
     */
    @PostMapping
    @Operation(summary = "상품권 즉시 교환", description = "포인트를 사용하여 상품권을 즉시 교환하고 핀번호를 받습니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "교환 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (포인트 부족 등)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ApiResponse<RewardResponseDTO>> exchangeReward(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "상품권 교환 정보", required = true)
            @Valid @RequestBody RewardRequestDTO requestDTO) {
        
        try {
            String token = authorization.replace("Bearer ", "");
            String username = jwtUtil.validateAndGetUsername(token);
            User user = userService.findByUsername(username);
            
            // 디버깅을 위한 로그 추가
            log.debug("상품권 교환 요청: rewardType={}, pointsUsed={}", 
                    requestDTO.getRewardType(), requestDTO.getPointsUsed());
            
            // 상품권 타입에 따른 포인트 자동 계산
            Integer requiredPoints = calculateRequiredPoints(requestDTO.getRewardType(), requestDTO.getPointsUsed());
            
            Reward reward = rewardService.exchangeRewardImmediately(
                    user, requiredPoints, requestDTO.getRewardType());
            RewardResponseDTO response = RewardResponseDTO.from(reward);
            
            return ResponseEntity.ok(ApiResponse.success("상품권 교환이 완료되었습니다", response));
            
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("상품권 교환 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("상품권 교환에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 현재 사용자의 상품권 교환 요청 목록을 조회합니다.
     * 
     * @param authorization JWT 토큰
     * @return 상품권 교환 요청 목록
     */
    @GetMapping
    @Operation(summary = "상품권 교환 요청 목록 조회", description = "현재 사용자의 상품권 교환 요청 목록을 최신순으로 조회합니다.")
    public ResponseEntity<ApiResponse<List<RewardResponseDTO>>> getMyRewards(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String authorization) {
        
        try {
            String token = authorization.replace("Bearer ", "");
            String username = jwtUtil.validateAndGetUsername(token);
            User user = userService.findByUsername(username);
            
            List<Reward> rewards = rewardService.getRewardsByUser(user);
            List<RewardResponseDTO> response = rewards.stream()
                    .map(RewardResponseDTO::from)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success("상품권 교환 요청 목록 조회 성공", response));
            
        } catch (Exception e) {
            log.error("상품권 교환 요청 목록 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("상품권 교환 요청 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 상품권 교환 요청을 상태별로 조회합니다.
     * 
     * @param authorization JWT 토큰
     * @param status 상품권 교환 상태 (PENDING/APPROVED/REJECTED)
     * @return 해당 상태의 상품권 교환 요청 목록
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "상품권 교환 요청 상태별 조회", description = "현재 사용자의 상품권 교환 요청을 상태별로 조회합니다.")
    public ResponseEntity<ApiResponse<List<RewardResponseDTO>>> getRewardsByStatus(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "상품권 교환 상태 (PENDING/APPROVED/REJECTED)", required = true)
            @PathVariable String status) {
        
        try {
            String token = authorization.replace("Bearer ", "");
            String username = jwtUtil.validateAndGetUsername(token);
            User user = userService.findByUsername(username);
            
            Reward.RewardStatus rewardStatus = Reward.RewardStatus.valueOf(status.toUpperCase());
            List<Reward> rewards = rewardService.getRewardsByUserAndStatus(user, rewardStatus);
            List<RewardResponseDTO> response = rewards.stream()
                    .map(RewardResponseDTO::from)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success("상품권 교환 요청 상태별 조회 성공", response));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("잘못된 상태값입니다: " + status));
        } catch (Exception e) {
            log.error("상품권 교환 요청 상태별 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("상품권 교환 요청 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 상품권 교환 요청을 기간별로 조회합니다.
     * 
     * @param authorization JWT 토큰
     * @param startDate 시작일시
     * @param endDate 종료일시
     * @return 해당 기간의 상품권 교환 요청 목록
     */
    @GetMapping("/date-range")
    @Operation(summary = "상품권 교환 요청 기간별 조회", description = "특정 기간의 상품권 교환 요청을 조회합니다.")
    public ResponseEntity<ApiResponse<List<RewardResponseDTO>>> getRewardsByDateRange(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "시작일시", required = true, example = "2025-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "종료일시", required = true, example = "2025-01-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            List<Reward> rewards = rewardService.getRewardsByDateRange(startDate, endDate);
            List<RewardResponseDTO> response = rewards.stream()
                    .map(RewardResponseDTO::from)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success("상품권 교환 요청 기간별 조회 성공", response));
            
        } catch (Exception e) {
            log.error("상품권 교환 요청 기간별 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("상품권 교환 요청 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 현재 사용자의 대기중인 상품권 교환 요청을 조회합니다.
     * 
     * @param authorization JWT 토큰
     * @return 대기중인 상품권 교환 요청 목록
     */
    @GetMapping("/pending")
    @Operation(summary = "대기중인 상품권 교환 요청 조회", description = "현재 사용자의 대기중인 상품권 교환 요청을 조회합니다.")
    public ResponseEntity<ApiResponse<List<RewardResponseDTO>>> getPendingRewards(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String authorization) {
        
        try {
            String token = authorization.replace("Bearer ", "");
            String username = jwtUtil.validateAndGetUsername(token);
            User user = userService.findByUsername(username);
            
            List<Reward> rewards = rewardService.getPendingRewardsByUser(user);
            List<RewardResponseDTO> response = rewards.stream()
                    .map(RewardResponseDTO::from)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success("대기중인 상품권 교환 요청 조회 성공", response));
            
        } catch (Exception e) {
            log.error("대기중인 상품권 교환 요청 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("상품권 교환 요청 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 상품권 교환 요청 상세 정보를 조회합니다.
     * 
     * @param authorization JWT 토큰
     * @param rewardId 조회할 상품권 교환 요청 ID
     * @return 상품권 교환 요청 상세 정보
     */
    @GetMapping("/{rewardId}")
    @Operation(summary = "상품권 교환 요청 상세 조회", description = "특정 상품권 교환 요청의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<RewardResponseDTO>> getRewardById(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "상품권 교환 요청 ID", required = true)
            @PathVariable Long rewardId) {
        
        try {
            Reward reward = rewardService.getRewardById(rewardId);
            RewardResponseDTO response = RewardResponseDTO.from(reward);
            
            return ResponseEntity.ok(ApiResponse.success("상품권 교환 요청 상세 조회 성공", response));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("상품권 교환 요청 상세 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("상품권 교환 요청 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 간단한 상품권 교환 API (GET 방식)
     * 상품권 타입만 지정하면 필요한 포인트가 자동 계산됩니다.
     * 
     * @param authorization JWT 토큰
     * @param rewardType 상품권 타입 (FIVE_THOUSAND, TEN_THOUSAND, THIRTY_THOUSAND)
     * @return 교환 결과
     */
    @GetMapping("/exchange/{rewardType}")
    @Operation(summary = "간편 상품권 교환 (GET)", description = "상품권 타입만 지정하여 간편하게 교환합니다. 포인트는 자동 계산됩니다.")
    public ResponseEntity<ApiResponse<RewardResponseDTO>> exchangeRewardSimple(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "상품권 타입 (FIVE_THOUSAND/TEN_THOUSAND/THIRTY_THOUSAND)", required = true)
            @PathVariable String rewardType) {
        
        try {
            String token = authorization.replace("Bearer ", "");
            String username = jwtUtil.validateAndGetUsername(token);
            User user = userService.findByUsername(username);
            
            // 상품권 타입에 따른 포인트 자동 계산
            Integer requiredPoints = calculateRequiredPoints(rewardType, null);
            
            log.info("간편 상품권 교환: 사용자={}, 타입={}, 포인트={}", 
                    username, rewardType, requiredPoints);
            
            Reward reward = rewardService.exchangeRewardImmediately(user, requiredPoints, rewardType);
            RewardResponseDTO response = RewardResponseDTO.from(reward);
            
            return ResponseEntity.ok(ApiResponse.success("상품권 교환이 완료되었습니다 (간편 교환)", response));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("간편 상품권 교환 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("상품권 교환에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 상품권 교환 가능 여부를 확인합니다.
     * 
     * @param authorization JWT 토큰
     * @param points 확인할 포인트 수량
     * @return 교환 가능 여부
     */
    @GetMapping("/check/{points}")
    @Operation(summary = "상품권 교환 가능 여부 확인", description = "특정 포인트로 상품권 교환이 가능한지 확인합니다.")
    public ResponseEntity<ApiResponse<Boolean>> checkRewardExchangeAvailability(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "확인할 포인트 수량", required = true)
            @PathVariable Integer points) {
        
        try {
            String token = authorization.replace("Bearer ", "");
            String username = jwtUtil.validateAndGetUsername(token);
            User user = userService.findByUsername(username);
            
            boolean canExchange = rewardService.canExchangeReward(user, points);
            String message = canExchange ? "상품권 교환이 가능합니다" : "보유 포인트가 부족합니다";
            
            return ResponseEntity.ok(ApiResponse.success(message, canExchange));
            
        } catch (Exception e) {
            log.error("상품권 교환 가능 여부 확인 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("상품권 교환 가능 여부 확인에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 상품권 타입에 따른 필요 포인트를 계산합니다.
     * 클라이언트에서 포인트를 명시하지 않은 경우, 상품권 타입에 따라 자동 계산합니다.
     * 
     * @param rewardType 상품권 타입
     * @param explicitPoints 클라이언트에서 명시한 포인트 (있는 경우)
     * @return 계산된 필요 포인트
     */
    private Integer calculateRequiredPoints(String rewardType, Integer explicitPoints) {
        // 클라이언트에서 명시적으로 포인트를 보낸 경우 그것을 사용
        if (explicitPoints != null && explicitPoints > 0) {
            log.debug("명시적 포인트 사용: {}", explicitPoints);
            return explicitPoints;
        }
        
        // 상품권 타입에 따른 자동 계산
        if (rewardType == null) {
            throw new IllegalArgumentException("상품권 타입을 입력해주세요.");
        }
        
        Integer calculatedPoints = switch (rewardType.toUpperCase()) {
            case "FIVE_THOUSAND" -> 5000;
            case "TEN_THOUSAND" -> 10000;
            case "THIRTY_THOUSAND" -> 30000;
            default -> {
                log.warn("알 수 없는 상품권 타입: {}. 기본값 5000pt 사용", rewardType);
                yield 5000;
            }
        };
        
        log.debug("상품권 타입 '{}'에 대해 계산된 포인트: {}", rewardType, calculatedPoints);
        return calculatedPoints;
    }

}
