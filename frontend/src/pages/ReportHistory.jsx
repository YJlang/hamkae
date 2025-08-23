import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../lib/authContext.jsx';
import { markerAPI } from '../lib/markerAPI';
import { getImageUrl } from '../lib/apiClient';
import Navbar from '../components/Navbar';

const ReportHistory = () => {
  const navigate = useNavigate();
  const { token } = useAuth();
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!token) {
      navigate('/login');
      return;
    }
    loadReportHistory();
  }, [token, navigate]);

  const loadReportHistory = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await markerAPI.getMyReports();
      const reportsData = response?.data || response || [];
      
      console.log('제보내역 데이터:', reportsData);
      setReports(reportsData);
      
    } catch (error) {
      console.error('제보내역 로드 실패:', error);
      setError('제보내역을 불러오는 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteMarker = async (markerId) => {
    if (!window.confirm('정말로 이 제보를 삭제하시겠습니까?\n삭제된 제보는 복구할 수 없습니다.')) {
      return;
    }

    try {
      setLoading(true);
      await markerAPI.delete(markerId);
      
      // 삭제 성공 후 목록에서 제거
      setReports(prevReports => prevReports.filter(report => report.id !== markerId));
      
      alert('제보가 성공적으로 삭제되었습니다.');
    } catch (error) {
      console.error('마커 삭제 실패:', error);
      alert('제보 삭제에 실패했습니다. 다시 시도해주세요.');
    } finally {
      setLoading(false);
    }
  };

  const getStatusText = (status) => {
    switch (status) {
      case 'ACTIVE': return '활성';
      case 'CLEANED': return '청소완료';
      case 'REMOVED': return '제거됨';
      default: return status;
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'ACTIVE': return 'text-orange-600 bg-orange-100';
      case 'CLEANED': return 'text-green-600 bg-green-100';
      case 'REMOVED': return 'text-gray-600 bg-gray-100';
      default: return 'text-gray-600 bg-gray-100';
    }
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

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#73C03F] mx-auto mb-4"></div>
          <p className="text-gray-600">제보내역을 불러오는 중...</p>
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
            onClick={loadReportHistory} 
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
          <h1 className="text-[28px] font-extrabold tracking-tight">제보내역</h1>
        </div>
      </div>

      {/* 본문 */}
      <div className="flex-1 overflow-auto bg-white rounded-t-[28px] -mt-10 px-6 pt-8 pb-24 shadow-md">
        {reports.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-gray-400 text-6xl mb-4">🗑️</div>
            <p className="text-gray-600 mb-2">아직 제보한 쓰레기가 없습니다</p>
            <p className="text-sm text-gray-400">지도에서 쓰레기를 발견하면 제보해보세요!</p>
            <button 
              onClick={() => navigate('/map')}
              className="mt-4 bg-[#73C03F] text-white px-6 py-2 rounded-lg hover:bg-[#5a9a2f] transition-colors"
            >
              지도로 이동
            </button>
          </div>
        ) : (
          <div className="space-y-4">
            <p className="text-sm text-gray-600 mb-4">총 {reports.length}개의 제보</p>
            
            {reports.map((report) => (
              <div key={report.id} className="bg-gray-50 rounded-xl p-4 border border-gray-200">
                <div className="flex justify-between items-start mb-3">
                  <div className="flex-1">
                    <p className="font-medium text-gray-900 mb-1">{report.description}</p>
                    <p className="text-sm text-gray-500">
                      {formatDate(report.createdAt)}
                    </p>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(report.status)}`}>
                      {getStatusText(report.status)}
                    </span>
                    {/* 삭제 버튼 - 활성 상태인 제보만 삭제 가능 */}
                    {report.status === 'ACTIVE' && (
                      <button
                        onClick={() => handleDeleteMarker(report.id)}
                        disabled={loading}
                        className="px-2 py-1 text-xs text-red-600 hover:text-red-700 hover:bg-red-50 rounded transition-colors"
                        title="제보 삭제"
                      >
                        🗑️
                      </button>
                    )}
                  </div>
                </div>
                
                {report.photos && report.photos.length > 0 && (
                  <div className="flex gap-2 overflow-x-auto pb-2">
                    {report.photos.map((photo) => (
                      <div key={photo.id} className="relative">
                        <img 
                          src={getImageUrl(photo.imagePath)} 
                          alt={`${photo.type === 'BEFORE' ? '제보' : '청소'} 사진`}
                          className="w-16 h-16 object-cover rounded-lg border border-gray-200"
                          onError={(e) => {
                            e.target.src = 'https://via.placeholder.com/150/cccccc/666666?text=이미지+없음';
                          }}
                          onLoad={(e) => {
                            e.target.classList.remove('animate-pulse', 'bg-gray-200');
                          }}
                        />
                        {photo.type && (
                          <span className={`absolute -top-1 -right-1 px-2 py-1 rounded-full text-xs font-medium ${getPhotoTypeColor(photo.type)}`}>
                            {getPhotoTypeText(photo.type)}
                          </span>
                        )}
                      </div>
                    ))}
                  </div>
                )}
                
                <div className="text-xs text-gray-400 mt-3">
                  위치: {report.lat?.toFixed(6)}, {report.lng?.toFixed(6)}
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

export default ReportHistory;
