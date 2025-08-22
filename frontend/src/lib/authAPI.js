import { api } from "./apiClient";

// 작업자명 : 윤준하
// 날짜 : 2025-08-14
// 수정내용 : API 응답 구조 주석 추가 및 에러 처리 개선

export const authAPI = {
  /**
   * 회원가입 API
   * @param {string} name - 사용자 이름
   * @param {string} username - 사용자 아이디
   * @param {string} password - 사용자 비밀번호
   * @returns {Promise<Object>} 응답 데이터
   * 응답 구조: { success: boolean, message: string, data: { user_id: number } }
   */
  register: async (name, username, password) => {
    try {
      const { data } = await api.post('/auth/register', { name, username, password });
      return data;
    } catch (error) {
      console.error('회원가입 API 에러:', error);
      throw error;
    }
  },

  /**
   * 로그인 API
   * @param {string} username - 사용자 아이디
   * @param {string} password - 사용자 비밀번호
   * @returns {Promise<Object>} 응답 데이터
   * 응답 구조: { success: boolean, message: string, data: { token: string, user: { id: number, name: string, points: number } } }
   */
  login: async (username, password) => {
    try {
      const { data } = await api.post('/auth/login', { username, password });
      return data;
    } catch (error) {
      console.error('로그인 API 에러:', error);
      throw error;
    }
  },
};
