import { api } from './apiClient';

export const markerAPI = {
  // 모든 활성 마커 조회
  list: (params = {}) => {
    console.log('마커 목록 요청:', params);
    return api.get("/markers", { params }).then((r) => {
      console.log('마커 목록 응답:', r.data);
      return r.data;
    });
  },

  // 특정 마커 상세 조회
  get: (id) => {
    console.log('마커 상세 조회 요청:', id);
    return api.get(`/markers/${id}`).then((r) => {
      console.log('마커 상세 응답:', r.data);
      return r.data;
    });
  },

  // 새 마커 생성
  create: (data) => {
    console.log('마커 생성 요청:', data);
    // FormData로 변환 (백엔드 @RequestParam 요구사항)
    const formData = new FormData();
    formData.append("lat", data.lat);
    formData.append("lng", data.lng);
    formData.append("description", data.description);

    // 이미지 파일들 추가
    if (data.images && data.images.length > 0) {
      data.images.forEach((image) => {
        if (image) {
          formData.append("images", image);
        }
      });
    }

    console.log('FormData로 변환된 요청:', {
      lat: data.lat,
      lng: data.lng,
      description: data.description,
      imageCount: data.images?.length || 0
    });

    return api.post("/markers", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    }).then((r) => {
      console.log('마커 생성 응답:', r.data);
      return r.data;
    });
  },

  // 마커 상태 변경 (AI 검증 성공 후 CLEANED 상태로 변경)
  updateStatus: (id, status) => {
    console.log('마커 상태 변경 요청:', { id, status });
    
    // URL 쿼리 파라미터로 status 전송
    const url = `/markers/${id}/status?status=${encodeURIComponent(status)}`;
    
    return api.patch(url).then((r) => {
      console.log('마커 상태 변경 응답:', r.data);
      return r.data;
    }).catch((error) => {
      console.error('마커 상태 변경 API 호출 실패:', error);
      console.error('요청 URL:', url);
      console.error('에러 상세:', {
        message: error.message,
        response: error.response?.data,
        status: error.response?.status
      });
      throw error;
    });
  },

  // 현재 사용자의 제보내역 조회
  getMyReports: () => {
    console.log('제보내역 조회 요청');
    return api.get('/markers/my-reports').then((r) => {
      console.log('제보내역 응답:', r.data);
      return r.data;
    }).catch((error) => {
      console.error('제보내역 조회 실패:', error);
      throw error;
    });
  },

  // 현재 사용자의 인증내역 조회 (청소 완료된 마커들)
  getMyVerifications: () => {
    console.log('인증내역 조회 요청');
    return api.get('/markers/my-verifications').then((r) => {
      console.log('인증내역 응답:', r.data);
      return r.data;
    }).catch((error) => {
      console.error('인증내역 조회 실패:', error);
      throw error;
    });
  },

  // 마커 삭제
  delete: (id) => {
    console.log('마커 삭제 요청:', id);
    return api.delete(`/markers/${id}`).then((r) => {
      console.log('마커 삭제 응답:', r.data);
      return r.data;
    }).catch((error) => {
      console.error('마커 삭제 실패:', error);
      throw error;
    });
  }
};