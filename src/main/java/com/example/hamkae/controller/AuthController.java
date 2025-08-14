package com.example.hamkae.controller;

import com.example.hamkae.config.JwtUtil;
import com.example.hamkae.domain.User;
import com.example.hamkae.DTO.ApiResponse;
import com.example.hamkae.DTO.LoginRequestDTO;
import com.example.hamkae.DTO.RegisterRequestDTO;
import com.example.hamkae.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 사용자 인증 관련 API를 처리하는 컨트롤러
 * 회원가입, 로그인 등의 인증 기능을 제공합니다.
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-12-19
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "인증 API", description = "사용자 회원가입 및 로그인 관련 API")
public class AuthController {

    /**
     * 사용자 관련 비즈니스 로직을 처리하는 서비스
     */
    private final UserService userService;
    
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
        } catch (RuntimeException e) {
            // 에러 발생 시 400 Bad Request로 응답
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
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
    }
}