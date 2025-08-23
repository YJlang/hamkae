import React from 'react';
import { useNavigate } from "react-router-dom";
import { authAPI } from '../lib/authAPI';
import { useAuth } from '../lib/authContext.jsx';
import { useState, useEffect } from "react";

const Login = ({ onLoginSuccess }) => {
    const navigate = useNavigate();
    const { login } = useAuth();
    
    const [formData, setFormData] = useState({
        username: '',
        password: '',
    });
      
    const [loading, setLoading] = useState(false);

    /* 
    작업자: 김혜린린
    수정내용: 로그인 폼 애니메이션 상태 추가 - 아래에서 위로 슬라이드 효과
    */
    const [isVisible, setIsVisible] = useState(false);

    useEffect(() => {
        // 컴포넌트 마운트 시 애니메이션 시작
        const timer = setTimeout(() => {
            setIsVisible(true);
        }, 100);
        
        return () => clearTimeout(timer);
    }, []);

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value,
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        console.log("로그인 시도:", formData);
        setLoading(true);

        try {
            // 작업자명 : 윤준하
            // 날짜 : 2025-08-14
            // 수정내용 : 백엔드 응답 구조에 맞게 토큰과 사용자 정보 저장 수정
            const response = await authAPI.login(formData.username, formData.password);
            
            if (response.success) {
                // 백엔드 응답 구조: { success: true, message: "로그인 성공", data: { token: "...", user: {...} } }
                const { token, user } = response.data;
                
                // 사용자명 우선순위: user.name > user.username > formData.username
                const displayName = user?.name || user?.username || formData.username;
                
                // useAuth를 통해 인증 상태 업데이트
                login(token, displayName);
                
                //김혜린 수정 2025-08-23
                // 로그인 성공 알림 및 콜백 호출
                alert('로그인이 완료되었습니다!');
                onLoginSuccess(displayName);
            } else {
                alert(response.message || '로그인에 실패했습니다.');
            }
            
        } catch (error) {
            console.error("로그인 에러:", error);
            if (error.response?.data?.message) {
                alert(error.response.data.message);
            } else {
                alert('로그인 중 오류가 발생했습니다.');
            }
        } finally {
            setLoading(false);
        }
    };
    return (
        <div>
            <div className='relative min-h-screen bg-[#73C03F]'>
                <div className='absolute bottom-30 inset-0 flex items-center justify-center pointer-events-none'>
                    <img src='/hamkae.png' alt='logo' className='w-40'/>
                </div>

                <div className="absolute bottom-0 left-0 right-0">
                    {/* 
                    작업자: 김혜린
                    날짜: 2025-08-23
                    수정내용: 로그인 폼에 애니메이션 클래스 추가 - 아래에서 위로 슬라이드 및 페이드인 효과
                    근데 될지는 모르니까 확인 필요. 안되면 삭제. 
                    */}
                    <div
                    className={`mx-auto w-full max-w-sm bg-white rounded-t-[28px] shadow-[0_-10px_30px_rgba(0,0,0,0.15)]
                                    px-5 pt-6 pb-8 transform transition-all duration-700 ease-out ${
                                        isVisible 
                                            ? 'translate-y-0 opacity-100' 
                                            : 'translate-y-full opacity-0'
                                }`}
                    style={{ paddingBottom: "calc(env(safe-area-inset-bottom, 0px) + 24px)" }}
                    >
                        <form onSubmit={handleSubmit}>
                        {/* 아이디 */}
                        <input
                            type="text"
                            placeholder="아이디를 입력해주세요."
                            className="auth-title mt-6 w-full bg-transparent text-gray-800 placeholder-[#73C03F]
                                    border-b-2 border-[#9bd07e] focus:border-[#73C03F] outline-none py-3"
                            value={formData.username}
                            onChange={handleChange}
                            name="username"
                        />

                        {/* 비밀번호 */}
                        <input
                            type="password"
                            name="password"
                            placeholder="비밀번호를 입력해주세요."
                            className="mb-4 mt-4 w-full bg-transparent text-gray-800 placeholder-[#73C03F]
                                    border-b-2 border-[#9bd07e] focus:border-[#73C03F] outline-none py-3"
                            value={formData.password}
                            onChange={handleChange}
                        />

                        {/* 버튼 */}
                        <div className="mt-6 grid grid-cols-2 gap-3">
                            <button
                            className="py-3 rounded-full bg-[#73C03F] text-white font-bold
                            transition-all duration-200
                            hover:bg-[#64AC37] hover:shadow-md
                            active:scale-95
                            focus:outline-none focus-visible:ring-2 focus-visible:ring-[#73C03F]
                            focus-visible:ring-offset-2 focus-visible:ring-offset-white"
                            type="submit"
                            disabled={loading}
                            >
                            {loading ? '로그인 중...' : '로그인'}
                            </button>
                            <button
                            className="py-3 rounded-full bg-[#A2D07A] text-white font-bold 
                            transition-all duration-200
                            hover:bg-[#89C25A] hover:shadow-md
                            active:scale-95
                            focus:outline-none focus-visible:ring-2 focus-visible:ring-[#A2D07A]
                            focus-visible:ring-offset-2 focus-visible:ring-offset-white"
                            type="button"
                            onClick={() => navigate("/Register")}
                            >
                            회원가입
                            </button>  
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Login;