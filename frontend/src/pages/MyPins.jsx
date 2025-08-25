import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../lib/authContext.jsx';
import { userAPI } from '../lib/userAPI';
import Navbar from '../components/Navbar';

const MyPins = () => {
  const navigate = useNavigate();
  const { token } = useAuth();
  const [pins, setPins] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState('all'); // 'all', 'available', 'used'
  const [revealedPins, setRevealedPins] = useState(new Set()); // 전체 핀번호가 표시된 핀번호들
  const [pinInfo, setPinInfo] = useState({}); // 핀번호별 상세 정보

  useEffect(() => {
    if (!token) {
      navigate('/login');
      return;
    }
    loadPins();
  }, [token, navigate, activeTab]);

  // 핀번호 자동 마스킹 타이머
  useEffect(() => {
    if (revealedPins.size > 0) {
      const timer = setTimeout(() => {
        setRevealedPins(new Set());
        setPinInfo({});
      }, 10000); // 10초 후 자동 마스킹

      return () => clearTimeout(timer);
    }
  }, [revealedPins]);

  const loadPins = async () => {
    try {
      setLoading(true);
      setError(null);
      
      let response;
      switch (activeTab) {
        case 'available':
          response = await userAPI.getAvailablePins();
          break;
        case 'used':
          response = await userAPI.getUsedPins();
          break;
        default:
          response = await userAPI.getMyPins();
      }
      
      const pinsData = response?.data || response || [];
      console.log('핀번호 데이터:', pinsData);
      setPins(pinsData);
      
    } catch (error) {
      console.error('핀번호 로드 실패:', error);
      setError('핀번호를 불러오는 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const togglePinReveal = async (pinId) => {
    if (revealedPins.has(pinId)) {
      // 이미 표시된 경우 마스킹
      setRevealedPins(prev => {
        const newSet = new Set(prev);
        newSet.delete(pinId);
        return newSet;
      });
      setPinInfo(prev => {
        const newInfo = { ...prev };
        delete newInfo[pinId];
        return newInfo;
      });
    } else {
      // 마스킹된 경우 전체 핀번호 표시
      try {
        const response = await userAPI.getFullPinInfo(pinId);
        
        if (response.success) {
          // 전체 핀번호 정보 저장
          setPinInfo(prev => ({
            ...prev,
            [pinId]: response.data
          }));
          
          // 표시 상태로 변경
          setRevealedPins(prev => new Set(prev).add(pinId));
          
          // 5초 후 자동 마스킹
          setTimeout(() => {
            setRevealedPins(prev => {
              const newSet = new Set(prev);
              newSet.delete(pinId);
              return newSet;
            });
            setPinInfo(prev => {
              const newInfo = { ...prev };
              delete newInfo[pinId];
              return newInfo;
            });
          }, 5000);
          
        } else {
          alert('핀번호 정보를 불러올 수 없습니다: ' + response.message);
        }
        
      } catch (error) {
        console.error('핀번호 정보 조회 실패:', error);
        
        if (error.response?.status === 400) {
          alert('핀번호 정보를 불러올 수 없습니다: ' + (error.response.data?.message || error.message));
        } else {
          alert('핀번호 정보를 불러오는 중 오류가 발생했습니다.');
        }
      }
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const getStatusText = (pin) => {
    if (pin.isUsed) return '사용완료';
    if (pin.isExpired) return '만료됨';
    if (pin.isAvailable) return '사용가능';
    return '상태불명';
  };

  const getStatusColor = (pin) => {
    if (pin.isUsed) return 'text-gray-600 bg-gray-100';
    if (pin.isExpired) return 'text-red-600 bg-red-100';
    if (pin.isAvailable) return 'text-green-600 bg-green-100';
    return 'text-gray-600 bg-gray-100';
  };

  const getRewardTypeDisplay = (rewardType) => {
    switch (rewardType) {
      case 'FIVE_THOUSAND':
        return '5000원 온누리상품권';
      case 'TEN_THOUSAND':
        return '10000원 온누리상품권';
      case 'THIRTY_THOUSAND':
        return '30000원 온누리상품권';
      default:
        return rewardType;
    }
  };

  const getRewardDescription = (rewardType) => {
    switch (rewardType) {
      case 'FIVE_THOUSAND':
        return '전통시장에서 사용 가능합니다!';
      case 'TEN_THOUSAND':
        return '전통시장에서 사용 가능합니다!';
      case 'THIRTY_THOUSAND':
        return '전통시장에서 사용 가능합니다!';
      default:
        return '상품권 사용 가능';
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#73C03F] mx-auto mb-4"></div>
          <p className="text-gray-600">핀번호를 불러오는 중...</p>
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
            onClick={loadPins} 
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
          <h1 className="text-[28px] font-extrabold tracking-tight">내 핀번호</h1>
        </div>
      </div>

      {/* 본문 */}
      <div className="flex-1 overflow-auto bg-white rounded-t-[28px] -mt-10 px-6 pt-8 pb-24 shadow-md">
        {/* 탭 메뉴 */}
        <div className="flex mb-6 bg-gray-100 rounded-lg p-1">
          <button
            onClick={() => setActiveTab('all')}
            className={`flex-1 py-2 px-4 rounded-md text-sm font-medium transition-colors ${
              activeTab === 'all' 
                ? 'bg-white text-[#73C03F] shadow-sm' 
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            전체
          </button>
          <button
            onClick={() => setActiveTab('available')}
            className={`flex-1 py-2 px-4 rounded-md text-sm font-medium transition-colors ${
              activeTab === 'available' 
                ? 'bg-white text-[#73C03F] shadow-sm' 
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            사용가능
          </button>
          <button
            onClick={() => setActiveTab('used')}
            className={`flex-1 py-2 px-4 rounded-md text-sm font-medium transition-colors ${
              activeTab === 'used' 
                ? 'bg-white text-[#73C03F] shadow-sm' 
                : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            사용완료
          </button>
        </div>

        {pins.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-gray-400 text-6xl mb-4">🎫</div>
            <p className="text-gray-600 mb-2">
              {activeTab === 'all' && '아직 발급된 핀번호가 없습니다'}
              {activeTab === 'available' && '사용 가능한 핀번호가 없습니다'}
              {activeTab === 'used' && '사용된 핀번호가 없습니다'}
            </p>
            <p className="text-sm text-gray-400">
              포인트 전환에서 상품권을 교환해보세요!
            </p>
            <button 
              onClick={() => navigate('/point-exchange')}
              className="mt-4 bg-[#73C03F] text-white px-6 py-2 rounded-lg hover:bg-[#5a9a2f] transition-colors"
            >
              포인트 전환하기
            </button>
          </div>
        ) : (
          <div className="space-y-4">
            <p className="text-sm text-gray-600 mb-4">
              총 {pins.length}개의 핀번호
            </p>
            
            {pins.map((pin) => (
              <div key={pin.id} className="bg-gradient-to-r from-yellow-50 to-orange-50 rounded-xl p-4 border border-yellow-200 shadow-sm">
                {/* 상품권 헤더 */}
                <div className="flex justify-between items-start mb-3">
                  <div className="flex-1">
                    <h3 className="font-bold text-lg text-orange-800 mb-1">
                      {getRewardTypeDisplay(pin.rewardType)}
                    </h3>
                    <p className="text-sm text-orange-600 mb-2">
                      {getRewardDescription(pin.rewardType)}
                    </p>
                  </div>
                  <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(pin)}`}>
                    {getStatusText(pin)}
                  </span>
                </div>
                
                {/* 핀번호 */}
                <div className="bg-white rounded-lg p-3 mb-3 border border-yellow-300">
                  <div className="flex justify-between items-center mb-1">
                    <p className="text-xs text-gray-500">핀번호</p>
                    <button
                      onClick={() => togglePinReveal(pin.id)}
                      className="text-xs text-[#73C03F] hover:text-[#5a9a2f] underline"
                    >
                      {revealedPins.has(pin.id) ? '숨기기' : '전체보기'}
                    </button>
                  </div>
                  <div 
                    className="font-mono text-lg font-bold text-gray-800 tracking-wider cursor-pointer hover:bg-gray-50 rounded p-2 transition-colors"
                    onClick={() => togglePinReveal(pin.id)}
                    title="클릭하여 전체 핀번호 보기"
                  >
                    {revealedPins.has(pin.id) ? (
                      <div>
                        <p className="text-green-600 mb-1">전체 핀번호 (5초 후 자동 숨김)</p>
                        <p className="text-lg">{pinInfo[pin.id]?.fullPinNumber || '****-****-****-****'}</p>
                      </div>
                    ) : (
                      <p>{pin.maskedPinNumber}</p>
                    )}
                  </div>
                  {revealedPins.has(pin.id) && (
                    <p className="text-xs text-orange-600 mt-2 text-center">
                      ⚠️ 보안을 위해 자동으로 마스킹됩니다
                    </p>
                  )}
                </div>
                
                {/* 상세 정보 */}
                <div className="grid grid-cols-2 gap-3 text-xs text-gray-600">
                  <div>
                    <span className="font-medium">발급일:</span>
                    <p>{formatDate(pin.issuedAt)}</p>
                  </div>
                  <div>
                    <span className="font-medium">만료일:</span>
                    <p>{formatDate(pin.expiresAt)}</p>
                  </div>
                  <div>
                    <span className="font-medium">사용 포인트:</span>
                    <p className="text-[#73C03F] font-medium">{pin.pointsUsed?.toLocaleString()}P</p>
                  </div>
                  {pin.isUsed && pin.usedAt && (
                    <div>
                      <span className="font-medium">사용일:</span>
                      <p>{formatDate(pin.usedAt)}</p>
                    </div>
                  )}
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

export default MyPins;
