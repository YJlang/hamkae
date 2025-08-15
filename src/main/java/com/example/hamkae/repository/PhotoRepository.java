package com.example.hamkae.repository;

import com.example.hamkae.domain.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Photo 엔티티를 위한 데이터 접근 계층 인터페이스
 * 사진 관련 데이터베이스 작업을 처리합니다.
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-13
 */
@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    /**
     * 특정 마커에 연결된 모든 사진을 조회합니다.
     * 
     * @param markerId 마커 ID
     * @return 해당 마커의 사진 목록
     */
    List<Photo> findByMarkerId(Long markerId);

    /**
     * 특정 사용자가 업로드한 모든 사진을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 해당 사용자의 사진 목록
     */
    List<Photo> findByUserId(Long userId);

    /**
     * 특정 마커의 특정 타입 사진들을 조회합니다.
     * 
     * @param markerId 마커 ID
     * @param type 사진 타입
     * @return 해당 마커의 특정 타입 사진 목록
     */
    List<Photo> findByMarkerIdAndType(Long markerId, Photo.PhotoType type);

    /**
     * 특정 마커의 특정 검증 상태 사진들을 조회합니다.
     * 
     * @param markerId 마커 ID
     * @param verificationStatus 검증 상태
     * @return 해당 마커의 특정 검증 상태 사진 목록
     */
    List<Photo> findByMarkerIdAndVerificationStatus(Long markerId, Photo.VerificationStatus verificationStatus);
}
