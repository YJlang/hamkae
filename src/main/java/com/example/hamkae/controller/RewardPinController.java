package com.example.hamkae.controller;

import com.example.hamkae.DTO.ApiResponse;
import com.example.hamkae.DTO.RewardPinResponseDTO;
import com.example.hamkae.config.JwtUtil;
import com.example.hamkae.domain.Reward;
import com.example.hamkae.domain.RewardPin;
import com.example.hamkae.domain.User;
import com.example.hamkae.service.RewardPinService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 상품권 핀번호 관련 API를 제공하는 컨트롤러 클래스
 * 핀번호 조회, 사용, 통계 등의 기능을 제공합니다.
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-01-14
 */
@RestController
@RequestMapping("/api/reward-pins")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "상품권 핀번호", description = "상품권 핀번호 관련 API")
public class RewardPinController {

    private final RewardPinService rewardPinService;
    private final RewardService rewardService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 현재 사용자의 모든 핀번호를 조회합니다.
     * 
     * @param authorization JWT 토큰
     * @return 핀번호 목록
     */
    @GetMapping
    @Operation(summary = "핀번호 목록 조회", description = "현재 사용자의 모든 상품권 핀번호를 최신순으로 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ApiResponse<List<RewardPinResponseDTO>>> getMyPins(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String authorization) {
        
        try {
            String token = authorization.replace("Bearer ", "");
            String username = jwtUtil.validateAndGetUsername(token);
            User user = userService.findByUsername(username);
            
            List<RewardPin> pins = rewardPinService.getPinsByUser(user);
            List<RewardPinResponseDTO> response = pins.stream()
                    .map(RewardPinResponseDTO::from)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success("핀번호 목록 조회 성공", response));
            
        } catch (Exception e) {
            log.error("핀번호 목록 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("핀번호 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 현재 사용자의 사용 가능한 핀번호를 조회합니다.
     * 
     * @param authorization JWT 토큰
     * @return 사용 가능한 핀번호 목록
     */
    @GetMapping("/available")
    @Operation(summary = "사용 가능한 핀번호 조회", description = "현재 사용자의 사용 가능한 상품권 핀번호를 조회합니다.")
    public ResponseEntity<ApiResponse<List<RewardPinResponseDTO>>> getAvailablePins(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String authorization) {
        
        try {
            String token = authorization.replace("Bearer ", "");
            String username = jwtUtil.validateAndGetUsername(token);
            User user = userService.findByUsername(username);
            
            List<RewardPin> pins = rewardPinService.getAvailablePinsByUser(user);
            List<RewardPinResponseDTO> response = pins.stream()
                    .map(RewardPinResponseDTO::from)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success("사용 가능한 핀번호 조회 성공", response));
            
        } catch (Exception e) {
            log.error("사용 가능한 핀번호 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용 가능한 핀번호 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 현재 사용자의 사용된 핀번호를 조회합니다.
     * 
     * @param authorization JWT 토큰
     * @return 사용된 핀번호 목록
     */
    @GetMapping("/used")
    @Operation(summary = "사용된 핀번호 조회", description = "현재 사용자의 사용된 상품권 핀번호를 조회합니다.")
    public ResponseEntity<ApiResponse<List<RewardPinResponseDTO>>> getUsedPins(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String authorization) {
        
        try {
            String token = authorization.replace("Bearer ", "");
            String username = jwtUtil.validateAndGetUsername(token);
            User user = userService.findByUsername(username);
            
            List<RewardPin> pins = rewardPinService.getUsedPinsByUser(user);
            List<RewardPinResponseDTO> response = pins.stream()
                    .map(RewardPinResponseDTO::from)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success("사용된 핀번호 조회 성공", response));
            
        } catch (Exception e) {
            log.error("사용된 핀번호 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용된 핀번호 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 특정 상품권 교환 요청의 핀번호를 조회합니다.
     * 
     * @param authorization JWT 토큰
     * @param rewardId 상품권 교환 요청 ID
     * @return 핀번호 정보
     */
    @GetMapping("/reward/{rewardId}")
    @Operation(summary = "상품권별 핀번호 조회", description = "특정 상품권 교환 요청의 핀번호를 조회합니다.")
    public ResponseEntity<ApiResponse<RewardPinResponseDTO>> getPinByReward(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "상품권 교환 요청 ID", required = true)
            @PathVariable Long rewardId) {
        
        try {
            String token = authorization.replace("Bearer ", "");
            String username = jwtUtil.validateAndGetUsername(token);
            User user = userService.findByUsername(username);
            
            Reward reward = rewardService.getRewardById(rewardId);
            
            // 본인의 상품권인지 확인
            if (!reward.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("본인의 상품권만 조회할 수 있습니다"));
            }
            
            Optional<RewardPin> pin = rewardPinService.getPinByReward(reward);
            if (pin.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("해당 상품권의 핀번호가 발급되지 않았습니다"));
            }
            
            // 새로 발급된 핀번호인 경우 실제 핀번호 제공, 그 외에는 마스킹
            RewardPinResponseDTO response = reward.isApproved() && pin.get().isAvailable() 
                    ? RewardPinResponseDTO.withFullPin(pin.get())
                    : RewardPinResponseDTO.from(pin.get());
            
            return ResponseEntity.ok(ApiResponse.success("핀번호 조회 성공", response));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("핀번호 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("핀번호 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 핀번호를 사용 처리합니다.
     * 
     * @param pinNumber 사용할 핀번호
     * @return 사용 처리 결과
     */
    @PostMapping("/use/{pinNumber}")
    @Operation(summary = "핀번호 사용", description = "상품권 핀번호를 사용 처리합니다.")
    public ResponseEntity<ApiResponse<RewardPinResponseDTO>> usePin(
            @Parameter(description = "사용할 핀번호", required = true)
            @PathVariable String pinNumber) {
        
        try {
            RewardPin usedPin = rewardPinService.usePin(pinNumber);
            RewardPinResponseDTO response = RewardPinResponseDTO.from(usedPin);
            
            return ResponseEntity.ok(ApiResponse.success("핀번호 사용 처리 완료", response));
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("핀번호 사용 처리 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("핀번호 사용 처리에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 핀번호 정보를 조회합니다.
     * 
     * @param pinNumber 조회할 핀번호
     * @return 핀번호 정보
     */
    @GetMapping("/info/{pinNumber}")
    @Operation(summary = "핀번호 정보 조회", description = "핀번호의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<RewardPinResponseDTO>> getPinInfo(
            @Parameter(description = "조회할 핀번호", required = true)
            @PathVariable String pinNumber) {
        
        try {
            Optional<RewardPin> pin = rewardPinService.getPinByPinNumber(pinNumber);
            if (pin.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("유효하지 않은 핀번호입니다"));
            }
            
            RewardPinResponseDTO response = RewardPinResponseDTO.from(pin.get());
            
            return ResponseEntity.ok(ApiResponse.success("핀번호 정보 조회 성공", response));
            
        } catch (Exception e) {
            log.error("핀번호 정보 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("핀번호 정보 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 특정 기간의 핀번호 발급 통계를 조회합니다.
     * 
     * @param authorization JWT 토큰
     * @param startDate 시작일시
     * @param endDate 종료일시
     * @return 해당 기간의 핀번호 목록
     */
    @GetMapping("/statistics")
    @Operation(summary = "핀번호 발급 통계", description = "특정 기간의 핀번호 발급 통계를 조회합니다.")
    public ResponseEntity<ApiResponse<List<RewardPinResponseDTO>>> getPinStatistics(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "시작일시", required = true, example = "2025-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "종료일시", required = true, example = "2025-01-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            List<RewardPin> pins = rewardPinService.getPinsByDateRange(startDate, endDate);
            List<RewardPinResponseDTO> response = pins.stream()
                    .map(RewardPinResponseDTO::from)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success("핀번호 발급 통계 조회 성공", response));
            
        } catch (Exception e) {
            log.error("핀번호 발급 통계 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("핀번호 발급 통계 조회에 실패했습니다: " + e.getMessage()));
        }
    }
}
