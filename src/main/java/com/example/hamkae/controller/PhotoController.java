package com.example.hamkae.controller;

import com.example.hamkae.DTO.ApiResponse;
import com.example.hamkae.config.JwtUtil;
import com.example.hamkae.repository.UserRepository;

import com.example.hamkae.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 사진 관련 API를 처리하는 컨트롤러 클래스
 * 청소 인증용 사진 업로드 및 사진 조회 기능을 제공합니다.
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-13
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/photos")
public class PhotoController {

    private final PhotoService photoService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    /**
     * 청소 인증용 사진을 업로드합니다.
     * 마커를 클릭한 후 청소 완료 사진을 업로드할 때 사용합니다.
     * 
     * @param markerId 마커 ID
     * @param image 업로드할 이미지 파일
     * @param authorization JWT 인증 토큰
     * @return 업로드된 사진 정보
     */
    @PostMapping("/upload/cleanup")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadCleanupPhoto(
            @RequestParam("marker_id") Long markerId,
            @RequestParam("image") MultipartFile image,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        // 인증 토큰 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(ApiResponse.error("인증 토큰이 필요합니다."));
        }

        String token = authorization.substring(7);
        String username = jwtUtil.validateAndGetUsername(token);
        if (username == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("유효하지 않은 토큰입니다."));
        }

        Long userId = userRepository.findByUsername(username)
                .map(u -> u.getId())
                .orElse(null);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("사용자를 찾을 수 없습니다."));
        }

        try {
            // 청소 인증용 사진 업로드 (자동으로 AFTER 타입)
            Long photoId = photoService.uploadCleanupPhoto(markerId, image, userId);
            
            // 응답 데이터 구성
            Map<String, Object> data = new HashMap<>();
            data.put("photo_id", photoId);
            data.put("marker_id", markerId);
            data.put("type", "AFTER");
            data.put("message", "청소 인증용 사진 업로드 완료");
            
            return ResponseEntity.ok(ApiResponse.success("청소 인증용 사진 업로드 완료", data));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("사진 업로드 실패: " + e.getMessage()));
        }
    }

    /**
     * 특정 사진의 상세 정보를 조회합니다.
     * 
     * @param id 조회할 사진 ID
     * @return 사진 상세 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPhotoById(@PathVariable Long id) {
        try {
            Map<String, Object> photoInfo = photoService.getPhotoById(id);
            return ResponseEntity.ok(ApiResponse.success("사진 조회 완료", photoInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("사진 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 특정 마커의 모든 사진을 조회합니다.
     * 
     * @param markerId 마커 ID
     * @return 해당 마커의 사진 목록
     */
    @GetMapping("/marker/{markerId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPhotosByMarkerId(@PathVariable Long markerId) {
        try {
            Map<String, Object> photosInfo = photoService.getPhotosByMarkerId(markerId);
            return ResponseEntity.ok(ApiResponse.success("마커별 사진 조회 완료", photosInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("마커별 사진 조회 실패: " + e.getMessage()));
        }
    }
}
