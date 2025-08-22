import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../lib/authContext.jsx';
import { markerAPI } from '../lib/markerAPI';
import { getImageUrl } from '../lib/apiClient';
import Navbar from '../components/Navbar';

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

  const loadVerificationHistory = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await markerAPI.getMyVerifications();
      const verificationsData = response?.data || response || [];
      
      console.log('인증내역 데이터:', verificationsData);
      setVerifications(verificationsData);
      
    } catch (error) {
      console.error('인증내역 로드 실패:', error);
      setError('인증내역을 불러오는 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
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
      <div className="flex-none relative px-6 pt-6 pb-14 text-white h-[120px]">
        <div className="flex items-center gap-3">
          <button 
            onClick={() => navigate(-1)} 
            className="text-2xl font-bold"
          >
            ←
          </button>
          <h1 className="text-[28px] font-extrabold tracking-tight">인증내역</h1>
        </div>
        <p className="text-sm opacity-90 mt-2">내가 청소 완료한 구역들</p>
      </div>

      {/* 본문 */}
      <div className="flex-1 overflow-auto bg-white rounded-t-[28px] -mt-10 px-6 pt-8 pb-24 shadow-md">
        {verifications.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-gray-400 text-6xl mb-4">🧹</div>
            <p className="text-gray-600 mb-2">아직 청소 인증한 구역이 없습니다</p>
            <p className="text-sm text-gray-400">제보된 쓰레기를 청소하고 인증해보세요!</p>
            <button 
              onClick={() => navigate('/map')}
              className="mt-4 bg-[#73C03F] text-white px-6 py-2 rounded-lg hover:bg-[#5a9a2f] transition-colors"
            >
              지도로 이동
            </button>
          </div>
        ) : (
          <div className="space-y-4">
            <p className="text-sm text-gray-600 mb-4">총 {verifications.length}개의 청소 완료</p>
            
            {verifications.map((verification) => (
              <div key={verification.id} className="bg-green-50 rounded-xl p-4 border border-green-200">
                <div className="flex justify-between items-start mb-3">
                  <div className="flex-1">
                    <p className="font-medium text-gray-900 mb-1">{verification.description}</p>
                    <p className="text-sm text-green-600 font-medium mb-1">✅ 청소 완료</p>
                    <p className="text-sm text-gray-500">
                      제보: {formatDate(verification.createdAt)}
                    </p>
                  </div>
                  <div className="text-right">
                    <div className="text-green-600 text-sm font-medium">+100P</div>
                    <div className="text-xs text-gray-500">포인트 적립</div>
                  </div>
                </div>
                
                {verification.photos && verification.photos.length > 0 && (
                  <div className="space-y-2 mb-3">
                    <p className="text-xs font-medium text-gray-600">사진 내역</p>
                    <div className="flex gap-2 overflow-x-auto pb-2">
                      {verification.photos.map((photo) => (
                        <div key={photo.id} className="flex flex-col items-center">
                          <img 
                            src={getImageUrl(photo.imagePath)} 
                            alt={`${getPhotoTypeText(photo.type)} 사진`}
                            onError={(e) => {
                              e.target.src = 'https://via.placeholder.com/100'; // 이미지 로딩 실패 시 대체 이미지
                            }}
                            className="w-16 h-16 object-cover rounded-lg border border-gray-200"
                          />
                          <span className={`px-2 py-1 rounded-full text-xs font-medium mt-1 ${getPhotoTypeColor(photo.type)}`}>
                            {getPhotoTypeText(photo.type)}
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
                
                <div className="text-xs text-gray-400">
                  위치: {verification.lat?.toFixed(6)}, {verification.lng?.toFixed(6)}
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