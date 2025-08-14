package com.example.hamkae.controller;

import com.example.hamkae.config.JwtUtil;
import com.example.hamkae.domain.User;
import com.example.hamkae.DTO.ApiResponse;
import com.example.hamkae.DTO.LoginRequestDTO;
import com.example.hamkae.DTO.RegisterRequestDTO;
import com.example.hamkae.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


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