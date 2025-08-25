import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../lib/authContext.jsx';
import { markerAPI } from '../lib/markerAPI';
import { getImageUrl } from '../lib/apiClient';
import Navbar from '../components/Navbar';
import { getAddressFromCoords } from '../lib/mapUtils'; // mapUtils에서 주소 조회 함수 import

const VerificationHistory = () => {
  const navigate = useNavigate();
  const { token } = useAuth();
  const [verifications, setVerifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!token) {
      navigate('/login');
      return;
    }
    loadVerificationHistory();
  }, [token, navigate]);

  // 주소가 없는 마커들에 대해 좌표로부터 주소를 조회하는 useEffect
  useEffect(() => {
    if (verifications.length > 0) {
      console.log('🔄 주소 조회 useEffect 실행:', verifications.length, '개 마커');
      
      verifications.forEach((verification, index) => {
        console.log(`📍 마커 ${index + 1} 분석:`, {
          id: verification.id,
          hasAddress: !!verification.address,
          hasCoordinates: !!(verification.lat && verification.lng),
          lat: verification.lat,
          lng: verification.lng
        });
        
        if (!verification.address && verification.lat && verification.lng) {
          console.log(`🚀 주소 조회 시작: 마커 ${verification.id}`);
          fetchAddressIfNeeded(verification);
        }
      });
    }
  }, [verifications]);

  const loadVerificationHistory = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await markerAPI.getMyVerifications();
      const verificationsData = response?.data || response || [];
      
      console.log('=== 인증내역 데이터 상세 분석 ===');
      console.log('원본 응답:', response);
      console.log('파싱된 데이터:', verificationsData);
      
      if (verificationsData.length > 0) {
        console.log('첫 번째 인증 데이터 상세:');
        const firstVerification = verificationsData[0];
        console.log('- 마커 ID:', firstVerification.id);
        console.log('- 설명:', firstVerification.description);
        console.log('- 주소:', firstVerification.address);
        console.log('- 상태:', firstVerification.status);
        console.log('- 생성일:', firstVerification.createdAt);
        console.log('- 수정일:', firstVerification.updatedAt);
        console.log('- 제보자:', firstVerification.reporter);
        
        if (firstVerification.photos && firstVerification.photos.length > 0) {
          console.log('- 사진 정보:');
          firstVerification.photos.forEach((photo, index) => {
            console.log(`  사진 ${index + 1}:`, {
              id: photo.id,
              type: photo.type,
              imagePath: photo.imagePath,
              gptResponse: photo.gptResponse,
              verifiedAt: photo.verifiedAt
            });
          });
        }
      }
      
      setVerifications(verificationsData);
      
    } catch (error) {
      console.error('인증내역 로드 실패:', error);
      setError('인증내역을 불러오는 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // GPT 응답을 파싱하는 함수 (더 강화된 버전)
  const parseGptResponse = (gptResponse) => {
    if (!gptResponse || typeof gptResponse !== 'string') {
      return null;
    }

    try {
      // 1단계: 기본 정리
      let cleanedResponse = gptResponse
        .replace(/\\n/g, ' ')           // 줄바꿈 제거
        .replace(/\\"/g, '"')           // 이스케이프된 따옴표 정리
        .replace(/\\t/g, ' ')          // 탭 제거
        .replace(/\\r/g, ' ')          // 캐리지 리턴 제거
        .replace(/\s+/g, ' ')          // 연속된 공백을 하나로
        .trim();                        // 앞뒤 공백 제거

      // 2단계: 여러 JSON 객체 찾기 (가장 깊은 중첩된 JSON 찾기)
      let bestJsonString = null;
      let maxDepth = 0;
      
      // 중괄호 쌍을 찾아서 가장 깊은 JSON 추출
      const findDeepestJson = (text) => {
        const stack = [];
        let start = -1;
        let depth = 0;
        
        for (let i = 0; i < text.length; i++) {
          if (text[i] === '{') {
            if (stack.length === 0) {
              start = i;
            }
            stack.push('{');
            depth = Math.max(depth, stack.length);
          } else if (text[i] === '}') {
            if (stack.length > 0) {
              stack.pop();
              if (stack.length === 0 && start !== -1) {
                // 완전한 JSON 객체 발견
                const jsonCandidate = text.substring(start, i + 1);
                try {
                  const parsed = JSON.parse(jsonCandidate);
                  // verification_result나 confidence가 있는지 확인
                  if (parsed.verification_result || parsed.confidence || parsed.reason) {
                    if (depth > maxDepth) {
                      maxDepth = depth;
                      bestJsonString = jsonCandidate;
                    }
                  }
                } catch (e) {
                  // 이 JSON은 파싱 실패, 계속 진행
                }
                start = -1;
              }
            }
          }
        }
      };
      
      findDeepestJson(cleanedResponse);
      
      if (!bestJsonString) {
        throw new Error('유효한 JSON을 찾을 수 없습니다');
      }
      
      console.log('🔍 추출된 JSON:', bestJsonString);
      
      // 3단계: 파싱 시도
      const parsed = JSON.parse(bestJsonString);
      
      // 4단계: 필요한 정보만 추출
      const result = {};
      
      // 신뢰도 처리
      if (parsed.confidence !== undefined) {
        result.confidence = Math.round(parsed.confidence * 100);
      }
      
      // 이유/설명 처리 (reason 필드 우선)
      if (parsed.reason) {
        result.reasoning = parsed.reason;
      } else if (parsed.reasoning) {
        result.reasoning = parsed.reasoning;
      } else if (parsed.explanation) {
        result.reasoning = parsed.explanation;
      } else if (parsed.comment) {
        result.reasoning = parsed.comment;
      }
      
      console.log('✅ 파싱 성공:', result);
      return result;
      
    } catch (error) {
      console.error('GPT 응답 파싱 실패:', error);
      console.log('원본 응답:', gptResponse);
      
      // 파싱 실패 시 간단한 텍스트 추출
      try {
        // JSON이 아닌 경우에도 유용한 정보 추출
        if (gptResponse.includes('APPROVED')) {
          return { verification_result: 'APPROVED', confidence: 90 };
        } else if (gptResponse.includes('REJECTED')) {
          return { verification_result: 'REJECTED', confidence: 0 };
        }
        
        // 신뢰도 숫자 추출
        const confidenceMatch = gptResponse.match(/(\d+\.?\d*)/);
        if (confidenceMatch) {
          const confidence = Math.round(parseFloat(confidenceMatch[1]) * 100);
          return { confidence, verification_result: 'UNKNOWN' };
        }
        
      } catch (fallbackError) {
        console.error('Fallback 파싱도 실패:', fallbackError);
      }
      
      return { 
        verification_result: 'UNKNOWN',
        raw: gptResponse.substring(0, 100) + '...'
      };
    }
  };

  // 한국 시간대로 변환하는 함수 (기존 formatDate 대체)
  const formatKoreanTime = (dateString) => {
    if (!dateString) return '-';
    
    try {
      const date = new Date(dateString);
      
      // 한국 시간대 (UTC+9)로 변환
      const koreanTime = new Date(date.getTime() + (9 * 60 * 60 * 1000));
      
      return koreanTime.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        timeZone: 'Asia/Seoul'
      });
    } catch (error) {
      console.error('시간 변환 실패:', error);
      return dateString;
    }
  };

  // 주소가 없을 때 좌표로부터 주소를 조회하는 함수 (mapUtils 사용)
  const fetchAddressIfNeeded = async (verification) => {
    if (verification.address) {
      console.log('📍 이미 주소가 있음:', verification.address);
      return;
    }
    
    if (!verification.lat || !verification.lng) {
      console.log('📍 좌표 정보 없음:', { lat: verification.lat, lng: verification.lng });
      return;
    }
    
    console.log('📍 주소 조회 시작 (mapUtils 사용):', { 
      id: verification.id, 
      lat: verification.lat, 
      lng: verification.lng 
    });
    
    try {
      // mapUtils의 getAddressFromCoords 함수 사용 (SDK 기반)
      const address = await getAddressFromCoords(verification.lat, verification.lng);
      if (address && address !== "주소를 찾을 수 없습니다." && address !== "주소 변환 중 오류가 발생했습니다.") {
        // 주소를 상태에 업데이트
        setVerifications(prev => prev.map(v => 
          v.id === verification.id 
            ? { ...v, address: address }
            : v
        ));
        console.log('✅ 주소 업데이트 완료 (SDK):', { id: verification.id, address });
      } else {
        console.log('⚠️ 주소 조회 실패 또는 유효하지 않은 주소:', address);
      }
    } catch (error) {
      console.error('❌ 주소 조회 실패 (SDK):', error);
    }
  };

  // GPT 응답을 카드 형태로 표시하는 컴포넌트
  const GptResponseCard = ({ gptResponse }) => {
    if (!gptResponse) return null;
    
    const parsedResponse = parseGptResponse(gptResponse);
    if (!parsedResponse) return null;
    
    // 주요 정보 추출
    const confidence = parsedResponse.confidence;
    const reasoning = parsedResponse.reasoning;
    
    return (
      <div className="bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200 rounded-xl p-3 my-2 shadow-sm">
        <h4 className="text-sm font-bold text-blue-800 mb-2 flex items-center gap-2">
          <span className="text-base">🤖</span>
          AI 검증 결과
        </h4>
        
        <div className="space-y-2">
          {/* 신뢰도 표시 - 모바일 최적화 */}
          {confidence && (
            <div className="space-y-1">
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-blue-700">신뢰도</span>
                <span className="text-xs font-bold text-blue-600 min-w-[2.5rem] text-right">
                  {confidence}%
                </span>
              </div>
              <div className="w-full bg-blue-200 rounded-full h-2.5">
                <div 
                  className="bg-blue-600 h-2.5 rounded-full transition-all duration-300 ease-out"
                  style={{ width: `${confidence}%` }}
                ></div>
              </div>
            </div>
          )}
          
          {/* AI 검증 이유 표시 - 모바일 최적화 */}
          {reasoning && (
            <div className="space-y-1">
              <span className="text-xs font-semibold text-blue-700">검증 이유</span>
              <div className="bg-white rounded-lg p-2 border border-blue-100">
                <p className="text-xs text-gray-800 leading-relaxed">
                  {reasoning}
                </p>
              </div>
            </div>
          )}
        </div>
      </div>
    );
  };

  const getPhotoTypeText = (type) => {
    switch (type) {
      case 'BEFORE': return '제보';
      case 'AFTER': return '청소';
      default: return type;
    }
  };

  const getPhotoTypeColor = (type) => {
    switch (type) {
      case 'BEFORE': return 'text-red-600 bg-red-100';
      case 'AFTER': return 'text-green-600 bg-green-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#73C03F] mx-auto mb-4"></div>
          <p className="text-gray-600">인증내역을 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="text-red-500 text-6xl mb-4">⚠️</div>
          <p className="text-gray-600 mb-4">{error}</p>
          <button 
            onClick={loadVerificationHistory} 
            className="bg-[#73C03F] text-white px-4 py-2 rounded-lg hover:bg-[#5a9a2f] transition-colors"
          >
            다시 시도
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col min-h-screen font-sans max-w-[375px] mx-auto" style={{ backgroundColor: '#73C03F' }}>
      {/* 상단 헤더 */}
      <div className="flex-none relative px-6 pt-6 pb-14 text-white h-[140px]">
        <h1 className="absolute top-7 left-7 flex items-center gap-1">
          <span className="text-[28px] font-extrabold tracking-tight">
            {getDisplayName()}
          </span>
          <span className="text-[14px] font-normal">님</span>
        </h1>
        <button 
          onClick={() => navigate(-1)} 
          className="absolute top-2 left-2 text-white text-xl"
        >
          ←
        </button>
        <img src="/logo.svg" alt="logo" className="absolute top-8 right-6 w-20 h-20" />
      </div>

      {/* 본문 */}
      <div className="flex-none overflow-auto bg-white rounded-t-[20px] -mt-10 px-6 pt-8 mb-10 shadow-md">
        인증내역
      </div>
      <div className="flex-1 overflow-auto bg-white px-6 pt-4 pb-4">
        {verifications.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-gray-400 text-6xl mb-4">📸</div>
            <p className="text-gray-600 mb-2">아직 청소 인증한 내역이 없습니다.</p>
            <p className="text-sm text-gray-400">쓰레기를 제보하고 청소 후 인증해보세요!</p>
          </div>
        ) : (
          <div className="space-y-4">
            {verifications.map((verification) => (
              <div key={verification.id} className="bg-white border border-gray-200 rounded-xl p-3 shadow-sm">
                {/* 마커 정보 */}
                <div className="mb-2">
                  <h3 className="font-semibold text-gray-900 mb-1 text-sm">
                    {verification.description || '위치 설명 없음'}
                  </h3>
                  <p className="text-xs text-gray-600 mb-1">
                    {verification.address ? (
                      verification.address
                    ) : (
                      <span className="text-red-500">⚠️ 주소 정보를 불러오는 중...</span>
                    )}
                  </p>
                  <p className="text-xs text-gray-500">
                    제보일: {formatKoreanTime(verification.createdAt)}
                  </p>
                </div>

                {/* 사진들 */}
                {verification.photos && verification.photos.length > 0 && (
                  <div className="mb-2">
                    <h4 className="text-sm font-medium text-gray-700 mb-2 flex items-center gap-2">
                      <span>📷</span>
                      <span>사진</span>
                    </h4>
                    <div className="grid grid-cols-2 gap-2">
                      {verification.photos.map((photo) => (
                        <div key={photo.id} className="relative">
                          <img
                            src={getImageUrl(photo.imagePath)}
                            alt={`${getPhotoTypeText(photo.type)} 사진`}
                            className="w-full h-20 object-cover rounded-lg"
                          />
                          <span className={`absolute top-1 left-1 px-1.5 py-0.5 rounded-full text-xs font-medium ${getPhotoTypeColor(photo.type)}`}>
                            {getPhotoTypeText(photo.type)}
                          </span>
                          
                          {/* GPT 응답 표시 (AFTER 타입 사진에만) */}
                          {photo.type === 'AFTER' && photo.gptResponse && (
                            <GptResponseCard gptResponse={photo.gptResponse} />
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* 상태 정보 */}
                <div className="flex items-center justify-between pt-2 border-t border-gray-100">
                  <span className="text-xs font-medium text-green-600 bg-green-100 px-2 py-1 rounded-full">
                    ✅ 청소 완료
                  </span>
                  <span className="text-xs text-gray-500">
                    인증일: {(() => {
                      // AI 검증 완료 시점이 있으면 그것을 사용, 없으면 마커 수정일 사용
                      const afterPhoto = verification.photos?.find(p => p.type === 'AFTER');
                      if (afterPhoto?.verifiedAt) {
                        return formatKoreanTime(afterPhoto.verifiedAt);
                      }
                      return formatKoreanTime(verification.updatedAt);
                    })()}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* 하단 네비게이션 */}
      <Navbar />
    </div>
  );
};

export default VerificationHistory;