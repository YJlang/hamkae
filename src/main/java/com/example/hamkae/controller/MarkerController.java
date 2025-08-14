package com.example.hamkae.controller;

import com.example.hamkae.DTO.ApiResponse;
import com.example.hamkae.DTO.MarkerRequestDTO;
import com.example.hamkae.DTO.MarkerResponseDTO;
import com.example.hamkae.config.JwtUtil;
import com.example.hamkae.repository.UserRepository;
import com.example.hamkae.service.FileUploadService;
import com.example.hamkae.service.MarkerService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * 마커 관련 API를 처리하는 컨트롤러 클래스
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-13
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/markers")

public class MarkerController {

    private final MarkerService markerService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;

    /**
     * 새로운 마커를 등록합니다 (사진 포함).
     * 
     * @param lat 위도
     * @param lng 경도
     * @param description 설명
     * @param images 업로드할 이미지 파일들
     * @param authorization JWT 인증 토큰
     * @return 등록된 마커의 ID와 사진 정보
     */
    @PostMapping

    public ResponseEntity<ApiResponse<Map<String, Object>>> registerMarker(
            @RequestParam("lat") String lat,
            @RequestParam("lng") String lng,
            @RequestParam("description") String description,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
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
            // 마커 등록
            MarkerRequestDTO markerRequest = MarkerRequestDTO.builder()
                    .lat(new java.math.BigDecimal(lat))
                    .lng(new java.math.BigDecimal(lng))
                    .description(description)
                    .build();
            
            Long markerId = markerService.registerMarker(markerRequest, userId);
            
            // 사진 업로드 및 마커 연결
            List<String> uploadedImagePaths = new ArrayList<>();
            if (images != null && images.length > 0) {
                for (MultipartFile image : images) {
                    if (!image.isEmpty()) {
                        String imagePath = fileUploadService.uploadImage(image);
                        uploadedImagePaths.add(imagePath);
                        
                        // 사진을 마커에 연결 (쓰레기 제보 사진은 자동으로 BEFORE 타입)
                        markerService.addPhotoToMarker(markerId, imagePath, "BEFORE", userId);
                    }
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("marker_id", markerId);
            data.put("uploaded_images", uploadedImagePaths);
            data.put("image_count", uploadedImagePaths.size());
            
            return ResponseEntity.ok(ApiResponse.success("마커 등록 완료", data));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("마커 등록 실패: " + e.getMessage()));
        }
    }

    /**
     * 모든 활성 마커를 조회합니다.
     * 
     * @return 활성 상태의 마커 목록
     */
    @GetMapping

    public ResponseEntity<ApiResponse<List<MarkerResponseDTO>>> getAllActiveMarkers() {
        List<MarkerResponseDTO> markers = markerService.getAllActiveMarkers();
        return ResponseEntity.ok(ApiResponse.success("마커 조회 완료", markers));
    }

    /**
     * 특정 마커의 상세 정보를 조회합니다.
     * 
     * @param id 조회할 마커 ID
     * @return 마커 상세 정보
     */
    @GetMapping("/{id}")

    public ResponseEntity<ApiResponse<MarkerResponseDTO>> getMarkerById(
            @PathVariable Long id) {
        MarkerResponseDTO marker = markerService.getMarkerById(id);
        return ResponseEntity.ok(ApiResponse.success("마커 상세 조회 완료", marker));
    }

    /**
     * 특정 사용자가 제보한 마커들을 조회합니다.
     * 
     * @param userId 제보자 사용자 ID
     * @return 해당 사용자가 제보한 마커 목록
     */
    @GetMapping("/user/{userId}")

    public ResponseEntity<ApiResponse<List<MarkerResponseDTO>>> getMarkersByUserId(
            @PathVariable Long userId) {
        List<MarkerResponseDTO> markers = markerService.getMarkersByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("사용자별 마커 조회 완료", markers));
    }

    /**
     * 마커를 삭제합니다 (제보자만 가능).
     * 
     * @param id 삭제할 마커 ID
     * @param authorization JWT 인증 토큰
     * @return 삭제 완료 메시지
     */
    @DeleteMapping("/{id}")

    public ResponseEntity<ApiResponse<String>> deleteMarker(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
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
            markerService.deleteMarker(id, userId);
            return ResponseEntity.ok(ApiResponse.success("마커와 연결된 모든 사진이 완전히 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("마커 삭제 실패: " + e.getMessage()));
        }
    }
}
