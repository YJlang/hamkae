import React, { useState } from 'react';
import { useAuth } from '../lib/authContext.jsx';
import { userAPI } from '../lib/userAPI.js';

const TestPage = () => {
  const { token, username, isAuthenticated } = useAuth();
  const [profileData, setProfileData] = useState(null);
  const [loading, setLoading] = useState(false);

  const testUserProfile = async () => {
    if (!token) {
      alert('로그인이 필요합니다.');
      return;
    }

    try {
      setLoading(true);
      const response = await userAPI.getProfile();
      setProfileData(response);
      console.log('프로필 테스트 결과:', response);
    } catch (error) {
      console.error('프로필 테스트 실패:', error);
      alert('프로필 조회 실패: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 p-6">
      <div className="max-w-2xl mx-auto bg-white rounded-lg shadow-md p-6">
        <h1 className="text-2xl font-bold text-gray-800 mb-6">🔍 사용자명 디버깅 테스트</h1>
        
        <div className="space-y-4">
          {/* 인증 상태 */}
          <div className="bg-blue-50 p-4 rounded-lg">
            <h2 className="text-lg font-semibold text-blue-800 mb-2">🔐 인증 상태</h2>
            <div className="space-y-2 text-sm">
              <p><strong>인증됨:</strong> {isAuthenticated ? '✅ 예' : '❌ 아니오'}</p>
              <p><strong>토큰:</strong> {token ? `✅ ${token.substring(0, 20)}...` : '❌ 없음'}</p>
              <p><strong>사용자명:</strong> {username ? `✅ ${username}` : '❌ 없음'}</p>
            </div>
          </div>

          {/* 로컬스토리지 */}
          <div className="bg-green-50 p-4 rounded-lg">
            <h2 className="text-lg font-semibold text-green-800 mb-2">💾 로컬스토리지</h2>
            <div className="space-y-2 text-sm">
              <p><strong>저장된 토큰:</strong> {localStorage.getItem('token') ? '✅ 있음' : '❌ 없음'}</p>
              <p><strong>저장된 사용자명:</strong> {localStorage.getItem('username') || '❌ 없음'}</p>
            </div>
          </div>

          {/* 백엔드 프로필 테스트 */}
          <div className="bg-yellow-50 p-4 rounded-lg">
            <h2 className="text-lg font-semibold text-yellow-800 mb-2">🌐 백엔드 프로필 테스트</h2>
            <button
              onClick={testUserProfile}
              disabled={loading || !token}
              className="px-4 py-2 bg-yellow-500 text-white rounded-lg hover:bg-yellow-600 disabled:opacity-50"
            >
              {loading ? '테스트 중...' : '프로필 조회 테스트'}
            </button>
            
            {profileData && (
              <div className="mt-4 p-3 bg-white rounded border">
                <h3 className="font-semibold mb-2">응답 데이터:</h3>
                <pre className="text-xs overflow-auto">
                  {JSON.stringify(profileData, null, 2)}
                </pre>
              </div>
            )}
          </div>

          {/* 사용자명 우선순위 테스트 */}
          <div className="bg-purple-50 p-4 rounded-lg">
            <h2 className="text-lg font-semibold text-purple-800 mb-2">🎯 사용자명 우선순위</h2>
            <div className="space-y-2 text-sm">
              <p><strong>1순위 (useAuth):</strong> {username || '❌ 없음'}</p>
              <p><strong>2순위 (백엔드 name):</strong> {profileData?.data?.name || profileData?.name || '❌ 없음'}</p>
              <p><strong>3순위 (백엔드 username):</strong> {profileData?.data?.username || profileData?.username || '❌ 없음'}</p>
              <p><strong>최종 표시명:</strong> {username || profileData?.data?.name || profileData?.data?.username || profileData?.name || profileData?.username || '사용자'}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TestPage;
