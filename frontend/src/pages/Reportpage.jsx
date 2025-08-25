import React, { useState, useMemo, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { markerAPI } from "../lib/markerAPI"; // markerAPI import 추가
import { getAddressFromCoords } from "../lib/mapUtils"; // getAddressFromCoords import 추가

const MAX_LEN = 800;
const MAX_FILES = 4;

const Reportpage = () => {
    const navigate = useNavigate();
    const [content, setContent] = useState("");
    const [files, setFiles] = useState(Array(MAX_FILES).fill(null));
    const [submitting, setSubmitting] = useState(false);
    const [location, setLocation] = useState(null); // 위치 정보 상태 추가
    const [address, setAddress] = useState("위치 정보를 가져오는 중..."); // 주소 상태 추가
    const [locationError, setLocationError] = useState(""); // 위치 정보 오류 상태 추가

    // 인증 가드: 토큰 없으면 로그인으로
    useEffect(() => {
        const token = localStorage.getItem('token');
        if (!token) navigate('/login');
    }, [navigate]);

    // 컴포넌트 마운트 시 현재 위치 가져오기 및 주소 변환
    useEffect(() => {
        const fetchLocationAndAddress = async (lat, lng) => {
            setLocation({ lat, lng });
            try {
                const fetchedAddress = await getAddressFromCoords(lat, lng);
                setAddress(fetchedAddress);
                setLocationError("");
            } catch (error) {
                console.error(error);
                setAddress("주소를 불러오는 데 실패했습니다.");
            }
        };

        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                (position) => {
                    fetchLocationAndAddress(position.coords.latitude, position.coords.longitude);
                },
                (error) => {
                    console.warn("Geolocation error:", error);
                    setLocationError("현재 위치를 가져올 수 없습니다. 기본 위치로 제보됩니다.");
                    // 기본 위치 설정 (성결대학교)
                    fetchLocationAndAddress(37.379, 126.929);
                }
            );
        } else {
            setLocationError("이 브라우저는 위치 서비스를 지원하지 않습니다. 기본 위치로 제보됩니다.");
            fetchLocationAndAddress(37.379, 126.929);
        }
    }, []);

    const previews = useMemo(
        () => files.map((f) => (f ? URL.createObjectURL(f) : null)),
        [files]
    );
    useEffect(() => () => previews.forEach((u) => u && URL.revokeObjectURL(u)), [previews]);

    const handleFileChange = (idx, e) => {
        const file = e.target.files?.[0];
        if (!file) return;
        
        // 모바일 환경 체크
        const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
        console.log(`📱 파일 선택 (${isMobile ? '모바일' : 'PC'}):`, file.name);
        
        // 파일 상세 정보 로깅
        console.log('🔍 파일 상세 정보:', {
            name: file.name,
            type: file.type,
            size: file.size,
            sizeMB: (file.size / (1024 * 1024)).toFixed(2),
            lastModified: new Date(file.lastModified).toLocaleString(),
            isMobile: isMobile,
            userAgent: navigator.userAgent
        });
        
        // 파일 타입 검증
        if (!/^image\//.test(file.type)) {
            alert("이미지만 업로드 가능합니다.");
            e.target.value = "";
            return;
        }
        
        // 파일 크기 검증 없음 (무제한 업로드 허용)
        // const maxSizeMB = 100;
        // const maxSizeBytes = maxSizeMB * 1024 * 1024;
        
        // if (file.size > maxSizeBytes) {
        //     const fileSizeMB = (file.size / (1024 * 1024)).toFixed(2);
        //     console.error(`❌ 파일 크기 초과: ${fileSizeMB}MB > ${maxSizeMB}MB`);
            
        //     // 모바일 카메라 사진인 경우 특별 안내
        //     if (isMobile && file.type.startsWith('image/')) {
        //         const shouldCompress = window.confirm(
        //             `모바일 카메라 사진이 너무 큽니다.\n\n` +
        //             `현재: ${fileSizeMB}MB\n` +
        //             `제한: ${maxSizeMB}MB\n\n` +
        //             `해결 방법:\n` +
        //             `1. 카메라 설정에서 해상도를 낮춰주세요\n` +
        //             `2. HDR 모드를 끄고 다시 촬영해주세요\n` +
        //             `3. 갤러리에서 기존 사진을 선택해주세요\n\n` +
        //             `계속 진행하시겠습니까? (파일이 제외됩니다)`
        //         );
                
        //         if (shouldCompress) {
        //             console.log('⚠️ 사용자가 큰 파일 업로드를 계속 진행');
        //             // 파일은 제외하지만 업로드 프로세스는 계속
        //             e.target.value = "";
        //             return;
        //         }
        //     }
            
        //     alert(`파일 용량이 너무 큽니다.\n현재: ${fileSizeMB}MB\n제한: ${maxSizeMB}MB\n\n사진을 다시 촬영하거나 압축해주세요.`);
        //     e.target.value = "";
        //     return;
        // }
        
        // 파일 크기 상세 로깅 (제한 없음)
        const fileSizeMB = (file.size / (1024 * 1024)).toFixed(2);
        console.log(`✅ 파일 검증 통과: ${file.name}`, {
            sizeBytes: file.size,
            sizeMB: fileSizeMB,
            maxSizeMB: '무제한',
            isUnderLimit: true,
            fileType: file.type,
            isMobile: isMobile
        });
        
        // 모바일 브라우저 최적화
        if (isMobile) {
            console.log('📱 모바일 디바이스 감지 - 파일 업로드 최적화 적용');
            
            // 모바일에서 파일 크기 제한 없음
            console.log(`✅ 모바일 파일 업로드: ${file.name} (${fileSizeMB}MB)`);
            
            // 모바일 카메라 사진 특별 처리
            if (file.type.startsWith('image/')) {
                console.log('📸 모바일 카메라 사진 감지 - 무제한 업로드 허용');
            }
        }
        
        // 파일명 길이 검증 (모바일에서 긴 파일명 문제 방지)
        if (file.name.length > 100) {
            alert("파일명이 너무 깁니다. 짧은 이름으로 변경해주세요.");
            e.target.value = "";
            return;
        }
        
        console.log(`✅ 파일 검증 통과: ${file.name}`, {
            type: file.type,
            size: file.size,
            lastModified: new Date(file.lastModified).toLocaleString()
        });
        
        setFiles((prev) => {
            const next = [...prev];
            next[idx] = file;
            return next;
        });
        
        // 모바일에서 파일 선택 후 입력 필드 초기화 (중복 선택 방지)
        if (isMobile) {
            setTimeout(() => {
                e.target.value = "";
            }, 100);
        }
    };

    const removeFile = (idx) => {
        setFiles((prev) => {
            const next = [...prev];
            next[idx] = null;
            return next;
        });
    };
    const filledCount = files.filter(Boolean).length;

    const onSubmit = async (e) => {
        e.preventDefault();
        if (!content.trim()) {
            alert("제보 내용을 입력해주세요.");
            return;
        }
        if (!location) {
            alert("위치 정보가 아직 준비되지 않았습니다. 잠시 후 다시 시도해주세요.");
            return;
        }

        setSubmitting(true);
        try {
            const imageFiles = files.filter(Boolean); // null이 아닌 파일만 필터링
            
            // 모바일 환경 체크 및 디버깅
            const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
            console.log('📱 모바일 환경 체크:', isMobile);
            console.log('📱 User Agent:', navigator.userAgent);
            console.log('📱 선택된 이미지 파일들:', imageFiles);
            
            // 이미지 파일 유효성 재검사 (파일 크기 제한 없음)
            // const maxSizeMB = 100;
            // const maxSizeBytes = maxSizeMB * 1024 * 1024;
            
            const validImages = imageFiles.filter(file => {
                if (!file || !file.type || !file.size) {
                    console.warn('❌ 유효하지 않은 파일:', file);
                    return false;
                }
                
                const isValidType = file.type.startsWith('image/');
                const isValidSize = true; // 파일 크기 제한 없음
                const fileSizeMB = (file.size / (1024 * 1024)).toFixed(2);
                
                console.log(`📁 파일 검증: ${file.name}`, {
                    type: file.type,
                    sizeBytes: file.size,
                    sizeMB: fileSizeMB,
                    maxSizeMB: '무제한',
                    isValidType,
                    isValidSize
                });
                
                return isValidType && isValidSize;
            });
            
            if (validImages.length !== imageFiles.length) {
                console.warn('⚠️ 일부 이미지 파일이 유효하지 않아 제외됨');
            }

            const reportData = {
                lat: String(location.lat),
                lng: String(location.lng),
                description: content.trim(),
                images: validImages,
            };
            console.log("📤 제보 데이터:", reportData);

            const response = await markerAPI.create(reportData);
            console.log("📥 백엔드 응답 전체:", response);
            
            const payload = response?.data || response;
            console.log("📦 응답 payload:", payload);
            
            const created = payload?.data || payload;
            console.log("✅ 생성된 마커 데이터:", created);
            
            //김혜린 수정 2025-08-23
            //검토 후 포인트가 지급됩니다. 메시지 삭제
            alert("제보가 접수되었습니다!");
            
            // 신규 마커 정보를 맵 페이지로 전달
            // 백엔드 응답에는 marker_id만 있고, lat/lng/description은 원본 reportData에서 가져와야 함
            const markerToPass = {
                id: created?.marker_id, // 백엔드 응답의 marker_id 사용
                lat: Number(reportData.lat), // 원본 reportData에서 lat 사용
                lng: Number(reportData.lng), // 원본 reportData에서 lng 사용
                description: reportData.description, // 원본 reportData에서 description 사용
                status: 'ACTIVE', // 새로 생성된 마커는 ACTIVE 상태
                photos: created?.uploaded_images || [] // 백엔드 응답의 uploaded_images 사용
            };
            console.log("🗺️ 맵으로 전달할 마커 데이터:", markerToPass);
            
            // 스크롤을 맨 위로 리셋하고 맵 페이지로 이동
            window.scrollTo(0, 0);
            navigate("/map", { state: { newMarker: markerToPass } });
        } catch (err) {
            console.error("❌ 제보 중 오류 발생:", err);
            
            // 모바일 환경에 맞는 상세한 에러 메시지
            let errorMessage = "제보 중 오류가 발생했습니다.";
            
            if (err.response) {
                // 서버 응답이 있는 경우
                const status = err.response.status;
                const data = err.response.data;
                
                console.error("🔍 서버 응답 상세:", { status, data });
                
                if (status === 400) {
                    errorMessage = "잘못된 요청입니다. 입력 정보를 확인해주세요.";
                } else if (status === 401) {
                    errorMessage = "로그인이 필요합니다. 다시 로그인해주세요.";
                } else if (status === 403) {
                    errorMessage = "권한이 없습니다. 관리자에게 문의하세요.";
                } else if (status === 413) {
                    errorMessage = "파일 크기 제한이 없습니다. 모든 크기의 사진을 업로드할 수 있습니다.";
                } else if (status === 500) {
                    errorMessage = "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
                } else if (data && data.message) {
                    errorMessage = data.message;
                }
            } else if (err.request) {
                // 요청은 보냈지만 응답을 받지 못한 경우
                console.error("🔍 네트워크 오류:", err.request);
                errorMessage = "네트워크 연결을 확인해주세요.";
            } else {
                // 기타 오류
                console.error("🔍 기타 오류:", err.message);
                if (err.message.includes('timeout')) {
                    errorMessage = "요청 시간이 초과되었습니다. 다시 시도해주세요.";
                } else if (err.message.includes('Network Error')) {
                    errorMessage = "네트워크 오류가 발생했습니다. 연결을 확인해주세요.";
                }
            }
            
            alert(errorMessage);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className='bg-[#73C03F] text-white'>
            <div className='flex'>
                <button onClick={() => navigate("/map")}>
                    <img src='/navigate-before.png' className='w-10 mt-5 ml-2'/>
                </button>
                <span className='font-bold mt-7 mr-2'>쓰레기 제보하기</span>
                <img src='/tresh.png' className='w-10 h-10 mt-4'/>
            </div>

            <div className='mt-10 p-2'>
                <p className='ml-2 mb-2'>현재위치</p>
                <div className='flex mb-5 items-center'>
                    <img src='/marker.png' className='ml-2 w-3 h-4 mr-2'/>
                    <span className='font-bold text-xl px-2 mr-2'>
                        {address}
                    </span>
                </div>
                {locationError && <p className='text-yellow-300 text-sm ml-3'>{locationError}</p>}
            </div>

            <div className='bg-white text-[#73C03F] rounded-t-3xl'>
                <form onSubmit={onSubmit} className='p-5'>
                    <span className='text-sm'>제보 내용</span>
                    <textarea 
                        value={content}
                        onChange={(e) => setContent(e.target.value.slice(0, MAX_LEN))}
                        placeholder='상황/위치/특이사항 등을 적어주세요.'
                                                className='mt-2 w-full h-40 rounded-xl border-solid border-2 border-[#73C03F] p-3 outline-none focus:ring-1 shadow text-black'
                    />
                    <div className='mt-1 text-sm text-gray-500 text-right'>
                        {content.length}/{MAX_LEN}
                    </div>

                    {/* 파일 업로드 안내 */}
                    <div className="mb-4 p-3 bg-blue-50 rounded-lg">
                        <h3 className="text-sm font-medium text-blue-800 mb-2">📱 모바일 사진 촬영 팁</h3>
                        <ul className="text-xs text-blue-700 space-y-1">
                            <li>• 카메라 해상도 제한 없음 - 원하는 대로 촬영하세요</li>
                            <li>• HDR 모드도 자유롭게 사용 가능합니다</li>
                            <li>• 파일 크기 제한이 없어 모든 사진을 업로드할 수 있습니다</li>
                            <li>• 문제가 지속되면 브라우저를 새로고침해보세요</li>
                        </ul>
                    </div>

                    {/* 파일업로드 */}
                    <div className="mb-4">
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            사진 업로드 (최대 3장)
                        </label>
                        <div className="grid grid-cols-3 gap-2">
                            {[0, 1, 2].map((idx) => (
                                <div key={idx} className="relative">
                                    <input
                                        type="file"
                                        accept="image/*"
                                        capture={idx === 0 ? "environment" : undefined}
                                        onChange={(e) => handleFileChange(idx, e)}
                                        className="hidden"
                                        id={`file-${idx}`}
                                    />
                                    <label
                                        htmlFor={`file-${idx}`}
                                        className={`block w-full h-24 border-2 border-dashed rounded-lg cursor-pointer transition-colors ${
                                            files[idx]
                                                ? 'border-green-500 bg-green-50'
                                                : 'border-gray-300 hover:border-[#73C03F] hover:bg-gray-50'
                                        }`}
                                    >
                                        <div className="flex flex-col items-center justify-center h-full text-center">
                                            {files[idx] ? (
                                                <>
                                                    <div className="text-green-600 text-xs font-medium">
                                                        {files[idx].name.length > 15 
                                                            ? files[idx].name.substring(0, 15) + '...' 
                                                            : files[idx].name
                                                        }
                                                    </div>
                                                    <div className="text-green-500 text-xs">
                                                        {(files[idx].size / (1024 * 1024)).toFixed(1)}MB
                                                    </div>
                                                </>
                                            ) : (
                                                <>
                                                    <div className="text-gray-500 text-xs">
                                                        {idx === 0 ? '📷 촬영' : '📁 선택'}
                                                    </div>
                                                    <div className="text-gray-400 text-xs">
                                                        {idx === 0 ? '카메라' : '갤러리'}
                                                    </div>
                                                </>
                                            )}
                                        </div>
                                    </label>
                                    {files[idx] && (
                                        <button
                                            type="button"
                                            onClick={() => removeFile(idx)}
                                            className="absolute -top-2 -right-2 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center text-xs hover:bg-red-600 transition-colors"
                                        >
                                            ×
                                        </button>
                                    )}
                                </div>
                            ))}
                        </div>
                        <p className="text-xs text-gray-500 mt-2">
                            첫 번째 버튼은 카메라로 촬영, 나머지는 갤러리에서 선택
                        </p>
                    </div>
                    
                    {/* 제출버튼 */}
                    <button
                        type="submit"
                        disabled={submitting || !location}
                        className="mt-6 w-full py-3 rounded-xl text-white font-bold disabled:opacity-60 bg-[#73C03F]"
                    >
                        {submitting ? "제출 중..." : "제보하기"}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default Reportpage;
