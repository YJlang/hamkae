import axios from 'axios';

// API 기본 URL 설정
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'https://hamkae.sku-sku.com'

// axios 인스턴스 생성
export const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000, // 30초 타임아웃
  withCredentials: true, // 쿠키 포함
});

// 요청 인터셉터 (토큰 자동 추가)
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    // 디버깅을 위한 로그
    console.log(`🌐 API 요청: ${config.method?.toUpperCase()} ${config.url}`, {
      baseURL: config.baseURL,
      fullURL: `${config.baseURL}${config.url}`,
      headers: config.headers
    });
    
    return config;
  },
  (error) => {
    console.error('❌ API 요청 인터셉터 오류:', error);
    return Promise.reject(error);
  }
);

// 응답 인터셉터 (에러 처리)
api.interceptors.response.use(
  (response) => {
    console.log(`✅ API 응답: ${response.config.url}`, {
      status: response.status,
      data: response.data
    });
    return response;
  },
  (error) => {
    console.error('❌ API 응답 오류:', {
      url: error.config?.url,
      method: error.config?.method,
      status: error.response?.status,
      message: error.message,
      response: error.response?.data
    });
    
    // 네트워크 오류 처리
    if (error.code === 'ERR_NETWORK') {
      console.error('🌐 네트워크 연결 오류 - 백엔드 서버가 실행 중인지 확인하세요');
    }
    
    // 401 인증 오류 처리
    if (error.response?.status === 401) {
      console.error('🔐 인증 오류 - 로그인이 필요합니다');
      localStorage.removeItem('token');
      // 로그인 페이지로 리다이렉트 (필요시)
    }
    
    return Promise.reject(error);
  }
);

// default export도 유지 (기존 코드와의 호환성)
export default api;

// 이미지 URL 생성 헬퍼 함수
export const getImageUrl = (imagePath) => {
  if (!imagePath) return '';
  if (typeof imagePath !== 'string') return '';
  
  // 이미 절대 URL인 경우 그대로 반환
  if (imagePath.startsWith('http')) return imagePath;
  
  // 백엔드 이미지 경로 처리 (업로드된 이미지)
  if (imagePath.startsWith('/images/')) {
    // nginx를 통해 이미지 접근
    // nginx: location /images/ { alias /var/www/hamkae/images/; }
    console.log('업로드된 이미지 경로:', imagePath);
    return `https://hamkae.sku-sku.com${imagePath}`;
  }
  
  // 정적 이미지 파일 (public 폴더의 이미지들)
  if (imagePath.startsWith('/public/')) {
    // /public/ 제거하고 루트에서 접근
    const staticPath = imagePath.replace('/public/', '/');
    console.log('정적 이미지 경로:', staticPath);
    return `https://hamkae.sku-sku.com${staticPath}`;
  }
  
  // 기타 상대 경로인 경우
  return `${API_BASE_URL}${imagePath}`;
};

// 이미지 로딩 실패 시 기본 이미지 반환
export const getImageUrlWithFallback = (imagePath, fallbackPath = '/tresh.png') => {
  try {
    if (!imagePath) return fallbackPath;
    
    const url = getImageUrl(imagePath);
    return url || fallbackPath;
  } catch (error) {
    console.error('이미지 URL 생성 실패:', error);
    return fallbackPath;
  }
};

// 이미지 로딩 상태 관리
export const createImageLoader = (fallbackPath = '/tresh.png') => {
  return {
    load: (imagePath) => {
      return new Promise((resolve, reject) => {
        if (!imagePath) {
          resolve(fallbackPath);
          return;
        }
        
        const img = new Image();
        img.onload = () => resolve(getImageUrl(imagePath));
        img.onerror = () => {
          console.warn(`이미지 로딩 실패: ${imagePath}, 기본 이미지 사용`);
          resolve(fallbackPath);
        };
        img.src = getImageUrl(imagePath);
      });
    }
  };
};