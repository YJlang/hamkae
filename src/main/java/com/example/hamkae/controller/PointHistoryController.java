package com.example.hamkae.controller;

import com.example.hamkae.DTO.ApiResponse;
import com.example.hamkae.DTO.PointHistoryResponseDTO;
import com.example.hamkae.DTO.PointStatisticsResponseDTO;
import com.example.hamkae.config.JwtUtil;
import com.example.hamkae.domain.PointHistory;
import com.example.hamkae.domain.User;
import com.example.hamkae.service.PointHistoryService;
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
import java.util.stream.Collectors;

/**
 * 포인트 이력 관련 API를 제공하는 컨트롤러 클래스
 * 포인트 이력 조회, 통계 조회 등의 기능을 제공합니다.
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-20
 */
@RestController
@RequestMapping("/api/point-history")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "포인트 이력", description = "포인트 적립/사용 이력 관련 API")
public class PointHistoryController {

    private final PointHistoryService pointHistoryService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 현재 사용자의 포인트 이력을 조회합니다.
     * 
     * @param authorization JWT 토큰
     * @return 포인트 이력 목록
     */
    @GetMapping
    @Operation(summary = "포인트 이력 조회", description = "현재 사용자의 포인트 적립/사용 이력을 최신순으로 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ApiResponse<List<PointHistoryResponseDTO>>> getPointHistories(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String authorization) {
        
        try {
            String token = authorization.replace("Bearer ", "");
            String username = jwtUtil.validateAndGetUsername(token);
            User user = userService.findByUsername(username);
            
            List<PointHistory> histories = pointHistoryService.getPointHistoriesByUser(user);
            List<PointHistoryResponseDTO> response = histories.stream()
                    .map(PointHistoryResponseDTO::from)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success("포인트 이력 조회 성공", response));
            
        } catch (Exception e) {
            log.error("포인트 이력 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("포인트 이력 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 현재 사용자의 포인트 이력을 타입별로 조회합니다.
     * 
     * @param authorization JWT 토큰
     * @param type 포인트 타입 (EARNED/USED)
     * @return 해당 타입의 포인트 이력 목록
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "포인트 이력 타입별 조회", description = "현재 사용자의 포인트 이력을 타입별로 조회합니다.")
    public ResponseEntity<ApiResponse<List<PointHistoryResponseDTO>>> getPointHistoriesByType(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "포인트 타입 (EARNED/USED)", required = true)
            @PathVariable String type) {
        
        try {
            String token = authorization.replace("Bearer ", "");
            String username = jwtUtil.validateAndGetUsername(token);
            User user = userService.findByUsername(username);
            
            PointHistory.PointType pointType = PointHistory.PointType.valueOf(type.toUpperCase());
            List<PointHistory> histories = pointHistoryService.getPointHistoriesByUserAndType(user, pointType);
            List<PointHistoryResponseDTO> response = histories.stream()
                    .map(PointHistoryResponseDTO::from)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success("포인트 이력 타입별 조회 성공", response));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("잘못된 포인트 타입입니다: " + type));
        } catch (Exception e) {
            log.error("포인트 이력 타입별 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("포인트 이력 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 현재 사용자의 포인트 이력을 기간별로 조회합니다.
     * 
     * @param authorization JWT 토큰
     * @param startDate 시작일시
     * @param endDate 종료일시
     * @return 해당 기간의 포인트 이력 목록
     */
    @GetMapping("/date-range")
    @Operation(summary = "포인트 이력 기간별 조회", description = "현재 사용자의 포인트 이력을 특정 기간으로 조회합니다.")
    public ResponseEntity<ApiResponse<List<PointHistoryResponseDTO>>> getPointHistoriesByDateRange(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "시작일시", required = true, example = "2025-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "종료일시", required = true, example = "2025-01-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            String token = authorization.replace("Bearer ", "");
            String username = jwtUtil.validateAndGetUsername(token);
            User user = userService.findByUsername(username);
            
            List<PointHistory> histories = pointHistoryService.getPointHistoriesByUserAndDateRange(user, startDate, endDate);
            List<PointHistoryResponseDTO> response = histories.stream()
                    .map(PointHistoryResponseDTO::from)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success("포인트 이력 기간별 조회 성공", response));
            
        } catch (Exception e) {
            log.error("포인트 이력 기간별 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("포인트 이력 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 현재 사용자의 포인트 통계를 조회합니다.
     * 
     * @param authorization JWT 토큰
     * @return 포인트 통계 정보
     */
    @GetMapping("/statistics")
    @Operation(summary = "포인트 통계 조회", description = "현재 사용자의 포인트 적립/사용 통계를 조회합니다.")
    public ResponseEntity<ApiResponse<PointStatisticsResponseDTO>> getPointStatistics(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String authorization) {
        
        try {
            String token = authorization.replace("Bearer ", "");
            String username = jwtUtil.validateAndGetUsername(token);
            User user = userService.findByUsername(username);
            
            PointHistoryService.PointStatistics statistics = pointHistoryService.getPointStatistics(user);
            PointStatisticsResponseDTO response = PointStatisticsResponseDTO.from(statistics);
            
            return ResponseEntity.ok(ApiResponse.success("포인트 통계 조회 성공", response));
            
        } catch (Exception e) {
            log.error("포인트 통계 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("포인트 통계 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 현재 사용자의 월별 포인트 적립 통계를 조회합니다.
     * 
     * @param authorization JWT 토큰
     * @param year 조회할 연도
     * @param month 조회할 월
     * @return 해당 월의 총 적립 포인트
     */
    @GetMapping("/monthly/{year}/{month}")
    @Operation(summary = "월별 포인트 적립 통계", description = "현재 사용자의 특정 월 포인트 적립 통계를 조회합니다.")
    public ResponseEntity<ApiResponse<Integer>> getMonthlyEarnedPoints(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "조회할 연도", required = true, example = "2025")
            @PathVariable int year,
            @Parameter(description = "조회할 월", required = true, example = "1")
            @PathVariable int month) {
        
        try {
            String token = authorization.replace("Bearer ", "");
            String username = jwtUtil.validateAndGetUsername(token);
            User user = userService.findByUsername(username);
            
            Integer monthlyPoints = pointHistoryService.getMonthlyEarnedPoints(user, year, month);
            
            return ResponseEntity.ok(ApiResponse.success("월별 포인트 적립 통계 조회 성공", monthlyPoints));
            
        } catch (Exception e) {
            log.error("월별 포인트 적립 통계 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("월별 포인트 통계 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 현재 사용자의 최근 N개 포인트 이력을 조회합니다.
     * 
     * @param authorization JWT 토큰
     * @param limit 조회할 개수
     * @return 최근 포인트 이력 목록
     */
    @GetMapping("/recent")
    @Operation(summary = "최근 포인트 이력 조회", description = "현재 사용자의 최근 N개 포인트 이력을 조회합니다.")
    public ResponseEntity<ApiResponse<List<PointHistoryResponseDTO>>> getRecentPointHistories(
            @Parameter(description = "JWT 토큰", required = true)
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "조회할 개수", required = false, example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            String token = authorization.replace("Bearer ", "");
            String username = jwtUtil.validateAndGetUsername(token);
            User user = userService.findByUsername(username);
            
            List<PointHistory> histories = pointHistoryService.getRecentPointHistories(user, limit);
            List<PointHistoryResponseDTO> response = histories.stream()
                    .map(PointHistoryResponseDTO::from)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success("최근 포인트 이력 조회 성공", response));
            
        } catch (Exception e) {
            log.error("최근 포인트 이력 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("포인트 이력 조회에 실패했습니다: " + e.getMessage()));
        }
    }
}
