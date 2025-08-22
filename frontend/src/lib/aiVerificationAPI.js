import { api } from './apiClient';

export const aiVerificationAPI = {
  // AI 검증 상태 조회
  getStatus: (markerId) => {
    console.log('AI 검증 상태 조회 요청:', markerId);
    return api.get(`/ai-verification/status/${markerId}`).then((r) => {
      console.log('AI 검증 상태 응답:', r.data);
      return r.data;
    }).catch((error) => {
      console.error('AI 검증 상태 조회 실패:', error);
      console.error('에러 상세:', {
        message: error.message,
        response: error.response?.data,
        status: error.response?.status
      });
      throw error;
    });
  },

  // 수동 AI 검증 수행
  verify: (markerId) => {
    console.log('수동 AI 검증 요청:', markerId);
    return api.post(`/ai-verification/verify/${markerId}`).then((r) => {
      console.log('AI 검증 응답:', r.data);
      return r.data;
    }).catch((error) => {
      console.error('AI 검증 요청 실패:', error);
      console.error('에러 상세:', {
        message: error.message,
        response: error.response?.data,
        status: error.response?.status,
        url: error.config?.url,
        method: error.config?.method
      });
      throw error;
    });
  },

  // 시스템 상태 확인
  getHealth: () => {
    return api.get('/ai-verification/health').then((r) => r.data);
  }
};
