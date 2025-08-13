package com.example.hamkae.repository;

import com.example.hamkae.domain.Marker;
import com.example.hamkae.domain.Marker.MarkerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 마커 데이터 접근을 위한 Repository 인터페이스
 *
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-13
 */
@Repository
public interface MarkerRepository extends JpaRepository<Marker, Long> {

    /**
     * 특정 사용자가 제보한 마커들을 조회합니다.
     *
     * @param userId 제보자 사용자 ID
     * @return 해당 사용자가 제보한 마커 목록
     */
    List<Marker> findByReportedById(Long userId);

    /**
     * 특정 상태의 마커들을 조회합니다.
     *
     * @param status 조회할 마커 상태
     * @return 해당 상태의 마커 목록
     */
    List<Marker> findByStatus(MarkerStatus status);

    /**
     * 활성 상태(ACTIVE)인 마커들만 조회합니다.
     *
     * @return 활성 상태의 마커 목록
     */
    @Query("SELECT m FROM Marker m WHERE m.status = 'ACTIVE' ORDER BY m.createdAt DESC")
    List<Marker> findActiveMarkers();

    /**
     * 특정 좌표 범위 내의 마커들을 조회합니다.
     *
     * @param minLat 최소 위도
     * @param maxLat 최대 위도
     * @param minLng 최소 경도
     * @param maxLng 최대 경도
     * @return 좌표 범위 내의 마커 목록
     */
    @Query("SELECT m FROM Marker m WHERE m.lat BETWEEN :minLat AND :maxLat AND m.lng BETWEEN :minLng AND :maxLng AND m.status = 'ACTIVE'")
    List<Marker> findMarkersInBounds(
            @Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLng") Double minLng,
            @Param("maxLng") Double maxLng
    );

    /**
     * 특정 사용자가 제보한 마커의 개수를 조회합니다.
     *
     * @param userId 제보자 사용자 ID
     * @return 해당 사용자가 제보한 마커 개수
     */
    long countByReportedById(Long userId);

    /**
     * 특정 상태의 마커 개수를 조회합니다.
     *
     * @param userId 제보자 사용자 ID
     * @return 해당 상태의 마커 개수
     */
    long countByStatus(MarkerStatus status);
}