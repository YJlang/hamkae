import React, { useEffect, useState } from 'react';

const TestMap = () => {
    const [status, setStatus] = useState('초기화 중...');
    const [error, setError] = useState(null);

    useEffect(() => {
        const testKakaoMaps = async () => {
            try {
                setStatus('Kakao Maps SDK 로딩 시작...');
                
                // 1. 환경변수 확인
                const apiKey = import.meta.env.VITE_KAKAO_JS_KEY;
                console.log('🔑 API Key:', apiKey);
                
                // 2. 스크립트 동적 로딩
                const script = document.createElement('script');
                script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${apiKey}&libraries=services&autoload=false`;
                script.async = true;
                
                script.onload = () => {
                    setStatus('스크립트 로드 완료, 초기화 중...');
                    console.log('✅ 스크립트 로드 완료');
                    console.log('🔍 window.kakao:', !!window.kakao);
                    
                    if (window.kakao && window.kakao.maps) {
                        window.kakao.maps.load(() => {
                            setStatus('Kakao Maps SDK 초기화 완료!');
                            console.log('✅ Kakao Maps SDK 초기화 완료');
                            console.log('🔍 최종 window.kakao 상태:', !!window.kakao);
                        });
                    } else {
                        setError('window.kakao 객체가 생성되지 않았습니다.');
                        setStatus('초기화 실패');
                    }
                };
                
                script.onerror = (e) => {
                    setError(`스크립트 로드 실패: ${e.message}`);
                    setStatus('로드 실패');
                    console.error('❌ 스크립트 로드 실패:', e);
                };
                
                document.head.appendChild(script);
                
            } catch (err) {
                setError(`오류 발생: ${err.message}`);
                setStatus('오류 발생');
                console.error('❌ 테스트 중 오류:', err);
            }
        };
        
        testKakaoMaps();
    }, []);

    return (
        <div className="p-4 bg-white rounded-lg shadow-md">
            <h2 className="text-xl font-bold mb-4">Kakao Maps SDK 테스트</h2>
            <div className="space-y-2">
                <p><strong>상태:</strong> {status}</p>
                <p><strong>도메인:</strong> {window.location.hostname}</p>
                <p><strong>프로토콜:</strong> {window.location.protocol}</p>
                {error && (
                    <p className="text-red-600"><strong>오류:</strong> {error}</p>
                )}
            </div>
        </div>
    );
};

export default TestMap;
