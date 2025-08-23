// [마이페이지 - 초기 디자인 롤백 + 필수 기능만 유지]
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../lib/authContext.jsx';
import { userAPI } from '../lib/userAPI';
import Navbar from '../components/Navbar';

const Mypage = () => {
  const navigate = useNavigate();
  const { token, logout, username: authUsername } = useAuth();
  const [userProfile, setUserProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showPointModal, setShowPointModal] = useState(false);
  const [newPoints, setNewPoints] = useState('');
  const [pointLoading, setPointLoading] = useState(false);

  // 인증 확인
  useEffect(() => {
    if (!token) {
      navigate('/login');
      return;
    }
    loadUserData();
  }, [token, navigate]);

  // 사용자명 결정 함수
  const getDisplayName = () => {
    // 1순위: useAuth의 username (로그인 시 저장된 값)
    if (authUsername) {
      return authUsername;
    }
    // 2순위: 백엔드에서 가져온 사용자명
    if (userProfile?.name) {
      return userProfile.name;
    }
    // 3순위: 백엔드에서 가져온 username
    if (userProfile?.username) {
      return userProfile.username;
    }
    // 4순위: 기본값
    return '사용자';
  };

  const loadUserData = async () => {
    try {
      setLoading(true);
      
      // 사용자 프로필과 포인트 현황을 병렬로 로드
      const [profileRes, pointsRes] = await Promise.all([
        userAPI.getProfile(),
        userAPI.getPointsSummary()
      ]);
      
      console.log('프로필 응답:', profileRes);
      console.log('포인트 현황 응답:', pointsRes);
      
      // 백엔드 응답 구조에 맞게 데이터 추출
      const profileData = profileRes?.data || profileRes;
      const pointsData = pointsRes?.data || pointsRes;
      
      // 사용자 프로필 정보 설정
      setUserProfile({
        ...profileData,
        // 포인트 정보는 별도 응답에서 가져옴
        currentPoints: pointsData?.currentPoints || profileData?.currentPoints || 0,
        totalEarnedPoints: pointsData?.totalEarnedPoints || profileData?.totalEarnedPoints || 0,
        totalUsedPoints: pointsData?.totalUsedPoints || profileData?.totalUsedPoints || 0
      });
      
      // 디버깅: 최종 설정된 사용자 프로필 정보 확인
      console.log('최종 설정된 사용자 프로필:', {
        ...profileData,
        currentPoints: pointsData?.currentPoints || profileData?.currentPoints || 0,
        totalEarnedPoints: pointsData?.totalEarnedPoints || profileData?.totalEarnedPoints || 0,
        totalUsedPoints: pointsData?.totalUsedPoints || profileData?.totalUsedPoints || 0
      });
      
    } catch (error) {
      console.error('사용자 데이터 로드 실패:', error);
      
      // 구체적인 에러 메시지 제공
      let errorMessage = '사용자 정보를 불러오는 중 오류가 발생했습니다.';
      
      if (error.response?.status === 401) {
        errorMessage = '로그인이 만료되었습니다. 다시 로그인해주세요.';
        logout();
        navigate('/login');
        return;
      } else if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.message) {
        errorMessage = `오류: ${error.message}`;
      }
      
      alert(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handlePointAdjustment = async () => {
    if (!newPoints || isNaN(newPoints) || newPoints < 0) {
      alert('유효한 포인트 값을 입력해주세요.');
      return;
    }

    try {
      setPointLoading(true);
      const response = await userAPI.setPointsForTesting(parseInt(newPoints));
      
      if (response.success) {
        alert(`포인트가 성공적으로 조정되었습니다!\n기존: ${response.data.oldPoints}P → 새로운: ${response.data.newPoints}P`);
        
        // 사용자 프로필 새로고침
        await loadUserData();
        
        // 모달 닫기
        setShowPointModal(false);
        setNewPoints('');
      } else {
        alert('포인트 조정에 실패했습니다: ' + response.message);
      }
    } catch (error) {
      console.error('포인트 조정 실패:', error);
      alert('포인트 조정에 실패했습니다: ' + (error.response?.data?.message || error.message));
    } finally {
      setPointLoading(false);
    }
  };

  // 초기 디자인은 상세 날짜/타입 색상 등이 필요 없으므로 보조 함수 제거

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#73C03F] mx-auto mb-4"></div>
          <p className="text-gray-600">사용자 정보를 불러오는 중...</p>
          <p className="text-sm text-gray-400 mt-2">잠시만 기다려주세요</p>
        </div>
      </div>
    );
  }

  // 사용자 데이터가 없는 경우 처리
  if (!userProfile) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="text-red-500 text-6xl mb-4">⚠️</div>
          <p className="text-gray-600 mb-4">사용자 정보를 불러올 수 없습니다.</p>
          <button 
            onClick={loadUserData} 
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
      {/* 상단 헤더 - 초기 디자인 */}
      <div className="flex-none relative px-6 pt-6 pb-14 text-white h-[120px]">
        <h1 className="absolute top-6 left-6 flex items-center gap-1">
          <span className="text-[28px] font-extrabold tracking-tight">
            {getDisplayName()}
          </span>
          <span className="text-[14px] font-normal">님</span>
        </h1>
        <img src="../../public/logo.svg" alt="캐릭터" className="absolute bottom-0 right-6 w-20 h-20" />
      </div>

      {/* 본문 - 초기 카드 레이아웃 */}
      <div className="flex-1 overflow-auto bg-white rounded-t-[28px] -mt-10 px-6 pt-8 pb-24 shadow-md flex flex-col">
        {/* 보유 포인트 */}
        <div className="mb-6">
          <div className="flex justify-between items-center mb-2">
            <p className="text-sm font-medium text-[#73C03F]">보유 포인트</p>
            <button 
              onClick={() => setShowPointModal(true)}
              className="text-xs text-[#73C03F] hover:text-[#5a9a2f] underline"
            >
              테스트용 조정
            </button>
          </div>
          <p className="text-4xl font-extrabold text-[#73C03F] leading-none">
            {userProfile?.currentPoints || 0} <span className="text-sm font-medium align-top">P</span>
          </p>
          
          {/* 포인트 상세 정보 (선택적 표시) */}
          {userProfile?.totalEarnedPoints > 0 && (
            <div className="mt-2 text-xs text-gray-500">
              <span>총 적립: {userProfile.totalEarnedPoints}P</span>
              {userProfile.totalUsedPoints > 0 && (
                <span className="ml-3">사용: {userProfile.totalUsedPoints}P</span>
              )}
            </div>
          )}
          
          <hr className="border-[#73C03F] border my-6" />
        </div>
          {/* 로그아웃 버튼 삭제. 김혜린 수정 2025-08-23 */}
        {/* 메뉴 버튼들 */}
        <div className="flex flex-col gap-3">
          <button onClick={() => navigate('/report-history')} className="w-full bg-[#73C03F] text-white rounded-xl py-4 font-medium flex justify-between items-center">
            <span className="pl-3">제보 내역</span><span className="text-2xl pr-2">{'>'}</span>
          </button>
          <button onClick={() => navigate('/verification-history')} className="w-full bg-[#73C03F] text-white rounded-xl py-4 font-medium flex justify-between items-center">
            <span className="pl-3">인증 내역</span><span className="text-2xl pr-2">{'>'}</span>
          </button>
          <button onClick={() => navigate('/my-pins')} className="w-full bg-[#73C03F] text-white rounded-xl py-4 font-medium flex justify-between items-center">
            <span className="pl-3">내 핀번호</span><span className="text-2xl pr-2">{'>'}</span>
          </button>
          <button onClick={() => navigate('/point-exchange')} className="w-full bg-[#73C03F] text-white rounded-xl py-4 font-medium flex justify-between items-center">
            <span className="pl-3">포인트 전환</span><span className="text-2xl pr-2">{'>'}</span>
          </button>
        </div>
      </div>

      {/* 포인트 조정 모달 */}
      {showPointModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-6 w-80 mx-4">
            <h3 className="text-lg font-bold text-gray-900 mb-4">테스트용 포인트 조정</h3>
            <p className="text-sm text-gray-600 mb-4">
              개발/테스트를 위한 포인트 조정 기능입니다.
            </p>
            
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                새로운 포인트
              </label>
              <input
                type="number"
                value={newPoints}
                onChange={(e) => setNewPoints(e.target.value)}
                placeholder="예: 10000"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#73C03F] focus:border-transparent"
                min="0"
              />
            </div>
            
            <div className="flex gap-3">
              <button
                onClick={() => {
                  setShowPointModal(false);
                  setNewPoints('');
                }}
                className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
              >
                취소
              </button>
              <button
                onClick={handlePointAdjustment}
                disabled={pointLoading || !newPoints}
                className="flex-1 px-4 py-2 bg-[#73C03F] text-white rounded-lg hover:bg-[#5a9a2f] transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {pointLoading ? '처리중...' : '적용'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 하단 네비게이션 */}
      <Navbar />
    </div>
  );
};

export default Mypage;
