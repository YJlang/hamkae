package com.example.hamkae.controller;

import com.example.hamkae.DTO.*;
import com.example.hamkae.config.JwtUtil;
import com.example.hamkae.domain.User;
import com.example.hamkae.repository.UserRepository;
import com.example.hamkae.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;


/**
 * 사용자 인증 및 사용자 정보 관련 API를 처리하는 컨트롤러
 * 회원가입, 로그인, 프로필 조회/수정 등의 기능을 제공합니다.
 * 
 * @author 권오윤
 * @version 1.0
 * @since 2025-08-14
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "인증 API", description = "사용자 회원가입 및 로그인 관련 API")
@Slf4j
public class AuthController {

    /**
     * 사용자 관련 비즈니스 로직을 처리하는 서비스
     */
    private final UserService userService;
    private final UserRepository userRepository;
    
    /**
     * JWT 토큰 생성 및 검증을 위한 유틸리티
     */
    private final JwtUtil jwtUtil;

    /**
     * 새로운 사용자 회원가입을 처리합니다.
     * 
     * @param request 회원가입 요청 정보 (이름, 아이디, 비밀번호)
     * @return 회원가입 결과 (성공 시 user_id 포함)
     */
    @PostMapping("/register")
    @Operation(
        summary = "회원가입",
        description = "새로운 사용자를 등록합니다. 이름, 아이디, 비밀번호가 필요합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "회원가입 성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청 (중복 아이디 등)",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@RequestBody RegisterRequestDTO request) {
        try {
            // 사용자 등록 처리
            User user = userService.register(request);
            
            // 응답 데이터 구성
            Map<String, Object> data = new HashMap<>();
            data.put("user_id", user.getId());
            
            return ResponseEntity.ok(ApiResponse.success("회원가입 성공", data));
        } catch (Exception e) {
            log.error("회원가입 실패: username={}, error={}", request.getUsername(), e.getMessage(), e);
            
            String errorMessage;
            if (e.getMessage() != null && e.getMessage().contains("이미 존재합니다")) {
                errorMessage = "이미 사용 중인 아이디입니다. 다른 아이디를 선택해주세요.";
            } else if (e.getMessage() != null && e.getMessage().contains("비밀번호")) {
                errorMessage = "비밀번호는 6자리 이상이어야 합니다.";
            } else if (e.getMessage() != null && e.getMessage().contains("사용자명")) {
                errorMessage = "사용자명은 2자리 이상이어야 합니다.";
            } else {
                errorMessage = "회원가입 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
            }
            
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
        }
    }

    /**
     * 사용자 로그인을 처리합니다.
     * 
     * @param request 로그인 요청 정보 (아이디, 비밀번호)
     * @return 로그인 결과 (성공 시 JWT 토큰과 사용자 정보 포함)
     */
    @PostMapping("/login")
    @Operation(
        summary = "로그인",
        description = "사용자 아이디와 비밀번호로 로그인하여 JWT 토큰을 발급받습니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "인증 실패 (잘못된 아이디/비밀번호)",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody LoginRequestDTO request) {
        // 사용자 정보 검증
        try {
            User user = userService.validateUser(request.getUsername(), request.getPassword());
            if (user == null) {
                // 로그인 실패 시 401 Unauthorized로 응답
                return ResponseEntity.status(401).body(ApiResponse.error("아이디 또는 비밀번호가 올바르지 않습니다."));
            }
            
            // JWT 토큰 생성
            String token = jwtUtil.createToken(user.getUsername());
            
            // 응답 데이터 구성
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            
            // 사용자 정보 구성
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("name", user.getName());
            userInfo.put("points", user.getPoints());
            data.put("user", userInfo);
            
            return ResponseEntity.ok(ApiResponse.success("로그인 성공", data));
        } catch (RuntimeException e) {
            log.error("로그인 실패: username={}, error={}", request.getUsername(), e.getMessage(), e);
            
            String errorMessage;
            if (e.getMessage() != null && e.getMessage().contains("사용자를 찾을 수 없습니다")) {
                errorMessage = "아이디 또는 비밀번호가 올바르지 않습니다.";
            } else if (e.getMessage() != null && e.getMessage().contains("비밀번호가 일치하지 않습니다")) {
                errorMessage = "아이디 또는 비밀번호가 올바르지 않습니다.";
            } else {
                errorMessage = "로그인 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
            }
            
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
        }
    }

    /**
     * 프로필 조회/수정 등의 기능을 제공합니다.
     *
     * @author 권오윤
     * @version 1.0
     * @since 2025-08-14
     */

    /**
     * 내 프로필 정보를 조회합니다.
     * Authorization: Bearer <JWT>
     */
    @GetMapping("/users/profile")
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> getMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        // JWT 토큰 검증 및 사용자 조회
        User user = getUserFromToken(authorization);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("인증에 실패했습니다."));
        }

        UserProfileResponseDTO dto = UserProfileResponseDTO.from(user);
        return ResponseEntity.ok(ApiResponse.success("프로필 조회 성공", dto));
    }

    /**
     * 사용자 프로필(이름)을 수정합니다.
     * 요청 본문: {"name": "string"}
     * 응답: {"success": true, "message": "프로필 수정 완료"}
     */
    @PutMapping("/users/profile")
    public ResponseEntity<ApiResponse<String>> updateMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody UserProfileUpdateRequestDTO request) {

        // JWT 토큰 검증 및 사용자 조회
        User user = getUserFromToken(authorization);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("인증에 실패했습니다."));
        }

        try {
            user.updateName(request.getName());
            userRepository.save(user);
            return ResponseEntity.ok(ApiResponse.success("프로필 수정 완료"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("프로필 수정에 실패했습니다."));
        }
    }

    /**
     * JWT 토큰에서 사용자 정보를 추출하는 공통 메서드입니다.
     * 
     * @param authorization Authorization 헤더 값
     * @return 인증된 사용자 객체, 인증 실패 시 null
     */
    private User getUserFromToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }

        String token = authorization.substring(7);
        String username = jwtUtil.validateAndGetUsername(token);
        if (username == null) {
            return null;
        }

        return userRepository.findByUsername(username).orElse(null);
    }
}