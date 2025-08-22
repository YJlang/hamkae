
const KAKAO_KEY = import.meta.env.VITE_KAKAO_JS_KEY;
const KAKAO_SDK_URL = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${KAKAO_KEY}&libraries=services&autoload=false`;

// 디버깅을 위한 로그 추가
console.log('🔍 Kakao Maps API Key:', KAKAO_KEY);
console.log('🔍 Kakao Maps SDK URL:', KAKAO_SDK_URL);
console.log('🔍 Current hostname:', window.location.hostname);

let isKakaoMapSdkLoaded = false;

const loadKakaoMapSdk = () => {
    return new Promise((resolve, reject) => {
        console.log('🚀 Kakao Maps SDK 로딩 시작...');
        
        // If already loaded, resolve immediately
        if (window.kakao && window.kakao.maps && window.kakao.maps.services) {
            console.log('✅ Kakao Maps SDK 이미 로드됨');
            isKakaoMapSdkLoaded = true;
            resolve(window.kakao);
            return;
        }
        
        // If script tag exists, it might be still loading
        const scriptId = 'kakao-map-sdk';
        let script = document.getElementById(scriptId);
        if (script && !isKakaoMapSdkLoaded) {
            console.log('📜 기존 스크립트 태그 발견, 로딩 대기...');
            script.addEventListener('load', () => {
                console.log('✅ 기존 스크립트 로드 완료');
                isKakaoMapSdkLoaded = true;
                window.kakao.maps.load(() => resolve(window.kakao));
            });
            script.addEventListener('error', (e) => {
                console.error('❌ 기존 스크립트 로드 실패:', e);
                reject(e);
            });
            return;
        }

        // Otherwise, create and append the script
        console.log('🔧 새로운 스크립트 태그 생성...');
        script = document.createElement("script");
        script.id = scriptId;
        script.src = KAKAO_SDK_URL;
        
        script.onload = () => {
            console.log('✅ Kakao Maps SDK script loaded successfully');
            console.log('🔍 window.kakao 객체 확인:', !!window.kakao);
            console.log('🔍 window.kakao.maps 확인:', !!(window.kakao && window.kakao.maps));
            
            if (!window.kakao || !window.kakao.maps) {
                console.error('❌ window.kakao 또는 window.kakao.maps가 존재하지 않음');
                reject(new Error('Kakao Maps SDK가 제대로 로드되지 않았습니다.'));
                return;
            }
            
            window.kakao.maps.load(() => {
                console.log('✅ Kakao Maps SDK initialized successfully');
                console.log('🔍 최종 window.kakao 상태:', !!window.kakao);
                isKakaoMapSdkLoaded = true;
                resolve(window.kakao);
            });
        };
        
        script.onerror = (e) => {
            console.error('❌ Kakao Maps SDK script failed to load:', e);
            console.error('❌ 스크립트 URL:', KAKAO_SDK_URL);
            reject(e);
        };

        console.log('📥 스크립트를 DOM에 추가:', script.src);
        document.head.appendChild(script);
    });
};

export const getAddressFromCoords = async (lat, lng) => {
    try {
        const kakao = await loadKakaoMapSdk();
        const geocoder = new kakao.maps.services.Geocoder();
        
        return new Promise((resolve, reject) => {
            geocoder.coord2Address(lng, lat, (result, status) => {
                if (status === kakao.maps.services.Status.OK) {
                    const address = result[0]?.road_address?.address_name || result[0]?.address?.address_name;
                    resolve(address || "주소를 찾을 수 없습니다.");
                } else {
                    reject(new Error("Failed to convert coordinates to address."));
                }
            });
        });
    } catch (error) {
        console.error("Kakao SDK/Geocoder error:", error);
        return "주소 변환 중 오류가 발생했습니다.";
    }
};
