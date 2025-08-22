import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate } from "react-router-dom";
import { markerAPI } from '../lib/markerAPI';
import { getImageUrl, getImageUrlWithFallback } from '../lib/apiClient';

const Maps = ({ newMarker }) => {
    const mapRef = useRef(null);
    const navigate = useNavigate();

    const [myLocation, setMyLocation] = useState(null);
    const [activeMarker, setActiveMarker] = useState(null);
    const [isMapLoading, setMapLoading] = useState(true);
    const [markers, setMarkers] = useState([]); // 마커 상태 관리

    // 디버깅을 위한 로그 추가
    useEffect(() => {
        console.log('🔍 Maps 컴포넌트 마운트됨');
        console.log('🔍 Kakao Maps API Key:', import.meta.env.VITE_KAKAO_JS_KEY);
        console.log('🔍 Current hostname:', window.location.hostname);
        console.log('🔍 window.kakao 존재 여부:', !!window.kakao);
    }, []);

    // 카카오맵 인스턴스를 저장하기 위한 ref
    const mapInstance = useRef({ map: null, infoWindow: null, clusterer: null });
    const locationWatchId = useRef(null);

    // 위치 업데이트 함수 (useCallback으로 최적화)
    const updateMyLocation = useCallback((position) => {
        const latlng = new window.kakao.maps.LatLng(position.coords.latitude, position.coords.longitude);
        setMyLocation(latlng);
        
        // 내 위치 마커 업데이트
        if (mapInstance.current.myMarker) {
            mapInstance.current.myMarker.setPosition(latlng);
        }
        
        console.log('위치 업데이트:', latlng);
    }, []);

    // 위치 에러 처리
    const handleLocationError = useCallback((error) => {
        console.warn('위치 업데이트 실패:', error);
        // 에러가 발생해도 기존 위치 유지
    }, []);

    // 마커 데이터 로딩 함수
    const loadMarkers = useCallback(async () => {
        try {
            console.log('마커 데이터 로딩 시작...');
            const response = await markerAPI.list({ page: 0, size: 200 });
            
            // 응답 구조 안전하게 처리
            const payload = response?.data || response;
            const pageData = payload?.data || payload;
            const apiMarkers = pageData?.content || pageData || [];
            
            console.log(`로딩된 마커 수: ${apiMarkers.length}개`);
            console.log('마커 데이터 샘플:', apiMarkers.slice(0, 2));
            
            setMarkers(apiMarkers);
            return apiMarkers;
        } catch (error) {
            console.error("마커 로딩 실패:", error);
            return [];
        }
    }, []);

    // 마커를 지도에 표시하는 함수
    const displayMarkers = useCallback((markersData) => {
        if (!mapInstance.current.clusterer || !markersData.length) return;
        
        try {
            // 기존 마커 제거
            mapInstance.current.clusterer.clear();
            
            const markerImg = new window.kakao.maps.MarkerImage(
                "/tresh.png", 
                new window.kakao.maps.Size(32, 32), 
                { offset: new window.kakao.maps.Point(16, 32) }
            );
            
            const markersForCluster = markersData
                .filter(markerData => {
                    const lat = Number(markerData.lat);
                    const lng = Number(markerData.lng);
                    return !Number.isNaN(lat) && !Number.isNaN(lng);
                })
                .map((markerData) => {
                    const lat = Number(markerData.lat);
                    const lng = Number(markerData.lng);
                    
                    const marker = new window.kakao.maps.Marker({
                        position: new window.kakao.maps.LatLng(lat, lng),
                        image: markerImg,
                    });
                    
                    // 클릭 이벤트 추가
                    window.kakao.maps.event.addListener(marker, 'click', async () => {
                        console.log('마커 클릭:', markerData);
                        
                        // 마커 클릭 시 상세 정보 조회 (사진 정보 포함)
                        try {
                            if (markerData.id) {
                                const detailResponse = await markerAPI.get(markerData.id);
                                const detailData = detailResponse?.data?.data || detailResponse?.data || detailResponse;
                                console.log('마커 상세 정보:', detailData);
                                
                                // 상세 정보로 activeMarker 설정
                                setActiveMarker(detailData);
                            } else {
                                setActiveMarker(markerData);
                            }
                        } catch (error) {
                            console.error('마커 상세 정보 조회 실패:', error);
                            setActiveMarker(markerData);
                        }
                    });
                    
                    return marker;
                });
            
            // 클러스터러에 마커 추가
            mapInstance.current.clusterer.addMarkers(markersForCluster);
            console.log(`지도에 ${markersForCluster.length}개 마커 표시 완료`);
            
        } catch (error) {
            console.error('마커 표시 중 오류:', error);
        }
    }, []);

    // 마커를 주기적으로 새로고침하는 함수
    const refreshMarkers = useCallback(async () => {
        try {
            console.log('마커 목록 새로고침 시작');
            const response = await markerAPI.list();
            const markersData = response?.data?.data || response?.data || response;
            
            if (Array.isArray(markersData)) {
                // AI 검증이 성공한 마커와 청소 완료된 마커는 제거
                const activeMarkers = markersData.filter(marker => 
                    marker.status !== 'APPROVED' && 
                    marker.status !== 'DELETED' && 
                    marker.status !== 'CLEANED'
                );
                
                console.log(`전체 마커: ${markersData.length}개, 활성 마커: ${activeMarkers.length}개`);
                console.log('숨겨진 마커 상태들:', markersData.filter(marker => 
                    marker.status === 'APPROVED' || 
                    marker.status === 'DELETED' || 
                    marker.status === 'CLEANED'
                ).map(m => ({ id: m.id, status: m.status })));
                
                // 마커 상태 업데이트
                setMarkers(activeMarkers);
                
                // 지도에 마커 표시
                displayMarkers(activeMarkers);
                
                // 현재 활성 마커가 숨겨진 경우 인포윈도우 닫기
                if (activeMarker && !activeMarkers.find(m => m.id === activeMarker.id)) {
                    setActiveMarker(null);
                }
            }
        } catch (error) {
            console.error('마커 새로고침 실패:', error);
        }
    }, [activeMarker, displayMarkers]);

    // 거리 계산 함수 (미터 단위)
    const calculateDistance = useCallback((lat1, lng1, lat2, lng2) => {
        const R = 6371000; // 지구 반지름 (미터)
        const dLat = (lat2 - lat1) * Math.PI / 180;
        const dLng = (lng2 - lng1) * Math.PI / 180;
        const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                  Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                  Math.sin(dLng/2) * Math.sin(dLng/2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }, []);

    // 1. 지도 초기화 Effect
    useEffect(() => {
        if (!window.kakao || !mapRef.current) return;

        window.kakao.maps.load(async () => {
            if (!mapRef.current) return;

            const initializeMap = async (center) => {
                try {
                    // 지도 생성
                    const map = new window.kakao.maps.Map(mapRef.current, { 
                        center, 
                        level: 4,
                        draggable: true,
                        scrollwheel: true
                    });
                    mapInstance.current.map = map;

                    // 마커 클러스터러 생성
                    const clusterer = new window.kakao.maps.MarkerClusterer({
                        map: map,
                        averageCenter: true,
                        minLevel: 6,
                        gridSize: 60,
                        disableClickZoom: false
                    });
                    mapInstance.current.clusterer = clusterer;

                    // 내 위치 마커 생성
                    const myMarker = new window.kakao.maps.Marker({ 
                        map, 
                        image: new window.kakao.maps.MarkerImage(
                            "/logo.svg", 
                            new window.kakao.maps.Size(24, 24), 
                            { offset: new window.kakao.maps.Point(12, 12) }
                        )
                    });
                    mapInstance.current.myMarker = myMarker;
                    myMarker.setPosition(center);

                    // 위치 추적 시작
                    if (navigator.geolocation) {
                        // 현재 위치 가져오기
                        navigator.geolocation.getCurrentPosition(
                            (pos) => updateMyLocation(pos),
                            handleLocationError,
                            { enableHighAccuracy: true, timeout: 10000, maximumAge: 30000 }
                        );
                        
                        // 위치 변화 감지
                        locationWatchId.current = navigator.geolocation.watchPosition(
                            updateMyLocation,
                            handleLocationError,
                            { enableHighAccuracy: true, timeout: 10000, maximumAge: 5000 }
                        );
                    }

                    // 마커 데이터 로딩 및 표시
                    const markersData = await loadMarkers();
                    displayMarkers(markersData);

                    // 공통 인포윈도우 생성
                    mapInstance.current.infoWindow = new window.kakao.maps.InfoWindow({ 
                        removable: true,
                        zIndex: 1000
                    });
                    
                    window.kakao.maps.event.addListener(
                        mapInstance.current.infoWindow, 
                        'closeclick', 
                        () => setActiveMarker(null)
                    );
                    window.kakao.maps.event.addListener(map, 'click', () => setActiveMarker(null));

                    setMapLoading(false);
                    console.log('지도 초기화 완료');
                    
                } catch (error) {
                    console.error('지도 초기화 중 오류:', error);
                    setMapLoading(false);
                }
            };

            // 현재 위치로 지도 초기화
            if (navigator.geolocation) {
                navigator.geolocation.getCurrentPosition(
                    (pos) => {
                        const currentLatLng = new window.kakao.maps.LatLng(
                            pos.coords.latitude, 
                            pos.coords.longitude
                        );
                        setMyLocation(currentLatLng);
                        initializeMap(currentLatLng);
                    },
                    (err) => {
                        console.warn("현재 위치를 가져올 수 없습니다. 기본 위치로 시작합니다.", err);
                        const defaultLatLng = new window.kakao.maps.LatLng(37.379, 126.929);
                        initializeMap(defaultLatLng);
                    },
                    { enableHighAccuracy: true, timeout: 10000, maximumAge: 30000 }
                );
            } else {
                console.warn("Geolocation을 지원하지 않습니다. 기본 위치로 시작합니다.");
                const defaultLatLng = new window.kakao.maps.LatLng(37.379, 126.929);
                initializeMap(defaultLatLng);
            }
        });

        // Cleanup
        return () => {
            if (locationWatchId.current && navigator.geolocation) {
                navigator.geolocation.clearWatch(locationWatchId.current);
            }
            if (mapInstance.current.infoWindow) {
                mapInstance.current.infoWindow.close();
            }
            if (mapInstance.current.clusterer) {
                mapInstance.current.clusterer.clear();
            }
        };
    }, [updateMyLocation, handleLocationError, loadMarkers, displayMarkers]);

    // 2. activeMarker 상태에 따라 인포윈도우를 열거나 닫는 Effect
    useEffect(() => {
        const { map, infoWindow } = mapInstance.current;
        if (!map || !infoWindow) return;

        if (activeMarker) {
            try {
                const position = new window.kakao.maps.LatLng(activeMarker.lat, activeMarker.lng);
                
                // 사진 정보 처리
                let photosInfo = '';
                if (activeMarker.photos && activeMarker.photos.length > 0) {
                    const photoCount = activeMarker.photos.length;
                    photosInfo = `
                        <div style="margin-bottom:8px; font-size:12px; color:#666;">
                            📸 제보 사진 ${photoCount}장
                        </div>
                    `;
                }
                
                // 주소 정보 생성 (좌표를 주소로 변환)
                let addressInfo = '';
                if (activeMarker.lat && activeMarker.lng) {
                    // 간단한 주소 표시 (좌표 기반)
                    addressInfo = `
                        <div style="margin-bottom:8px; font-size:12px; color:#666; border-top: 1px solid #eee; padding-top: 8px;">
                            📍 위치: ${activeMarker.lat.toFixed(6)}, ${activeMarker.lng.toFixed(6)}
                        </div>
                    `;
                }
                
                // 안전한 이미지 URL 생성
                let imageUrl = '/tresh.png'; // 기본 이미지
                let hasValidImage = false;
                
                if (activeMarker.photos && 
                    activeMarker.photos.length > 0 && 
                    activeMarker.photos[0] && 
                    activeMarker.photos[0].imagePath) {
                    
                    const imagePath = activeMarker.photos[0].imagePath;
                    try {
                        if (typeof imagePath === 'string' && imagePath.startsWith("/images")) {
                            imageUrl = getImageUrlWithFallback(imagePath, '/tresh.png');
                            hasValidImage = true;
                        } else if (typeof imagePath === 'string') {
                            imageUrl = getImageUrlWithFallback(imagePath, '/tresh.png');
                            hasValidImage = true;
                        }
                    } catch (error) {
                        console.warn('이미지 URL 생성 실패, 기본 이미지 사용:', error);
                        imageUrl = '/tresh.png';
                        hasValidImage = false;
                    }
                }
                
                infoWindow.setContent(`
                    <div style="width:280px; padding:12px; font-family: 'Pretendard', sans-serif;">
                        <div style="display:flex; gap:12px; align-items:flex-start; margin-bottom:10px;">
                            <img src="${imageUrl}" alt="제보사진" style="width:80px;height:80px;object-fit:cover;border-radius:8px;border:2px solid ${hasValidImage ? '#73C03F' : '#ddd'};" />
                            <div style="flex:1; min-width:0;">
                                <div style="font-weight:700; color:#2f6c1e; margin-bottom:4px;">
                                    ${activeMarker.user?.name || '익명'}님이 제보
                                </div>
                                <div style="font-size:13px; color:#333; margin-bottom:4px;">
                                    📝 ${activeMarker.description || '설명 없음'}
                                </div>
                                ${photosInfo}
                                ${addressInfo}
                            </div>
                        </div>
                        <div id="distance-info" style="font-size:12px; margin-bottom:8px; text-align:center; font-weight:500; height:16px;"></div>
                        <button id="clean-btn" style="width:100%; background:#73C03F; color:white; border:none; border-radius:8px; padding:10px 0; font-weight:700; cursor:pointer; font-size:15px; transition: all 0.2s;">
                            거리 계산 중...
                        </button>
                    </div>
                `);
                
                const markerForInfoWindow = new window.kakao.maps.Marker({ position });
                infoWindow.open(map, markerForInfoWindow);
                
                setTimeout(() => {
                    const btn = document.getElementById('clean-btn');
                    if(btn && activeMarker.id) {
                        btn.onclick = () => navigate(`/upload/${activeMarker.id}`);
                    }
                }, 100);
                
            } catch (error) {
                console.error('인포윈도우 생성 중 오류:', error);
                // 에러 발생 시 기본 인포윈도우 표시
                const addressInfo = activeMarker.lat && activeMarker.lng ? 
                    `<div style="font-size:12px; color:#666; margin-top:4px; border-top: 1px solid #eee; padding-top: 4px;">
                        📍 위치: ${activeMarker.lat.toFixed(6)}, ${activeMarker.lng.toFixed(6)}
                    </div>` : '';
                
                infoWindow.setContent(`
                    <div style="width:200px; padding:10px;">
                        <div style="font-weight:700; color:#2f6c1e;">마커 정보</div>
                        <div style="font-size:13px; color:#333; margin-top:4px;">
                            📝 ${activeMarker.description || '설명 없음'}
                        </div>
                        ${addressInfo}
                    </div>
                `);
                const position = new window.kakao.maps.LatLng(activeMarker.lat, activeMarker.lng);
                const markerForInfoWindow = new window.kakao.maps.Marker({ position });
                infoWindow.open(map, markerForInfoWindow);
            }
        } else {
            infoWindow.close();
        }
    }, [activeMarker, navigate]);

    // 3. 사용자의 위치나 activeMarker가 바뀔 때마다 버튼 상태를 업데이트하는 Effect
    useEffect(() => {
        if (!myLocation || !activeMarker) return;

        const distance = calculateDistance(
            myLocation.getLat(), 
            myLocation.getLng(), 
            activeMarker.lat, 
            activeMarker.lng
        );
        const isEnabled = distance >= 0 && distance <= 1000;

        const btn = document.getElementById('clean-btn');
        const distInfo = document.getElementById('distance-info');
        if (!btn || !distInfo) return;

        btn.disabled = !isEnabled;
        if (isEnabled) {
            btn.style.opacity = '1';
            btn.style.cursor = 'pointer';
            btn.innerText = '청소하러 가기';
            distInfo.innerHTML = ` ${Math.round(distance)}m 거리, 청소 가능!`;
            distInfo.style.color = '#2f6c1e';
        } else {
            btn.style.opacity = '0.6';
            btn.style.cursor = 'not-allowed';
            btn.innerText = '청소하러 갈 수 없는 거리입니다';
            distInfo.style.color = '#dc3545';
            distInfo.style.marginBottom = '2px';
            if (distance > 500) {
                distInfo.innerHTML = `너무 멉니다 (${Math.round(distance)}m). 500m 이하로 접근해주세요.`;
            } else if (distance < 5) {
                distInfo.innerHTML = `너무 가깝습니다 (${Math.round(distance)}m). 5m 이상 떨어져주세요.`;
            }
        }
    }, [myLocation, activeMarker, calculateDistance]);

    // 4. 새 마커 추가 Effect
    useEffect(() => {
        if (!newMarker) return;
        
        console.log('=== 신규 마커 처리 시작 ===');
        console.log('newMarker:', newMarker);
        
        const addMarkerToMap = (markerData) => {
            try {
                // 좌표 유효성 검사
                const lat = Number(markerData.lat);
                const lng = Number(markerData.lng);
                
                if (Number.isNaN(lat) || Number.isNaN(lng)) {
                    console.error('유효하지 않은 좌표:', markerData.lat, markerData.lng);
                    return;
                }

                // 마커 생성 및 추가
                const markerImg = new window.kakao.maps.MarkerImage("/tresh.png", new window.kakao.maps.Size(32, 32), { offset: new window.kakao.maps.Point(16, 32) });
                const marker = new window.kakao.maps.Marker({
                    position: new window.kakao.maps.LatLng(lat, lng),
                    image: markerImg,
                });
                
                // 클릭 이벤트 추가
                window.kakao.maps.event.addListener(marker, 'click', () => setActiveMarker(markerData));
                
                // 클러스터러에 마커 추가
                mapInstance.current.clusterer.addMarker(marker);
                
                // 지도 중심 이동 및 줌 레벨 조정
                const newPosition = new window.kakao.maps.LatLng(lat, lng);
                mapInstance.current.map.setCenter(newPosition);
                mapInstance.current.map.setLevel(3);
                
                console.log('신규 마커 추가 성공:', lat, lng);
                
                // 자동으로 마커 선택 (1초 후)
                setTimeout(() => setActiveMarker(markerData), 1000);
                
            } catch (error) {
                console.error('마커 추가 중 오류:', error);
            }
        };

        const processMarker = async () => {
            // 지도가 준비되지 않았으면 1초 후 재시도
            if (!mapInstance.current.map || !mapInstance.current.clusterer) {
                console.log('지도 초기화 대기 중... 1초 후 재시도');
                setTimeout(processMarker, 1000);
                return;
            }
            
            try {
                // 1. 좌표가 직접 있으면 바로 추가
                if (newMarker.lat && newMarker.lng) {
                    console.log('좌표로 직접 마커 추가:', newMarker.lat, newMarker.lng);
                    addMarkerToMap(newMarker);
                    return;
                }
                
                // 2. ID가 있으면 상세 조회 후 추가
                const id = newMarker.id || newMarker.marker_id;
                if (id) {
                    console.log('마커 ID로 상세 조회:', id);
                    const res = await markerAPI.get(id);
                    const markerData = res?.data?.data || res?.data || res;
                    
                    if (markerData && markerData.lat && markerData.lng) {
                        addMarkerToMap(markerData);
                    } else {
                        console.error('상세 조회 결과에 좌표가 없음:', markerData);
                    }
                } else {
                    console.error('신규 마커에 ID나 좌표가 없음:', newMarker);
                }
            } catch (error) {
                console.error('마커 처리 실패:', error);
            }
        };
        
        processMarker();
    }, [newMarker]);

    // 5. 마커 자동 새로고침 (AI 검증 상태 확인)
    useEffect(() => {
        const interval = setInterval(() => {
            refreshMarkers();
        }, 30000); // 30초마다 새로고침

        return () => clearInterval(interval);
    }, [refreshMarkers]);

    return (
        <div>
            <div ref={mapRef} className='w-full h-[100vh]'>
                {isMapLoading && (
                    <div style={{ 
                        position: 'absolute', 
                        top: 0, 
                        left: 0, 
                        width: '100%', 
                        height: '100%', 
                        background: 'rgba(255,255,255,0.9)', 
                        zIndex: 10, 
                        display: 'flex', 
                        justifyContent: 'center', 
                        alignItems: 'center', 
                        color: "#73C03F",
                        fontSize: '18px',
                        fontWeight: 'bold'
                    }}>
                        <div style={{ textAlign: 'center' }}>
                            <div>지도를 불러오는 중입니다...</div>
                            <div style={{ fontSize: '14px', marginTop: '8px', color: '#666' }}>
                                마커 {markers.length}개 로딩됨
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Maps;
