import Navbar from '../components/Navbar';
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../lib/authContext.jsx';

const Mainpage = () => {
    const navigate = useNavigate();
    const { username, isAuthenticated, logout } = useAuth();
    const [displayName, setDisplayName] = useState('사용자');
    
    // 사용자명 설정
    useEffect(() => {
        if (username) {
            setDisplayName(username);
        } else if (isAuthenticated) {
            // 토큰은 있지만 username이 없는 경우 (백엔드에서 사용자 정보 재조회)
            setDisplayName('사용자');
        } else {
            setDisplayName('사용자');
        }
    }, [username, isAuthenticated]);

    // 로그아웃 처리
    const handleLogout = () => {
        if (window.confirm('로그아웃 하시겠습니까?')) {
            logout();
            navigate('/login');
        }
    };

    const places = [
        {
            id: 1,
            title: "안양시 만안구 성결대학교 44",
            address: "내 집 앞 쓰레기...",
            imageUrl: "sample/tresh-1.jpg",
            point: 300,
            distance: 8, // m 단위 예시
        },
        {
            id: 2,
            title: "경기도 안양시 동안구 관양동 53",
            address: "골목가 쓰레기 더미",
            imageUrl: "sample/tresh-1.jpg",
            point: 300,
            distance: 25,
        },
        {
            id: 3,
            title: "경기도 안양시 만안구 성결…",
            address: "계단 앞 무단투기",
            imageUrl: "sample/tresh-1.jpg",
            point: 300,
            distance: 5,
        },
    ];

    return (
        <div>
            <div className="bg-white min-h-screen text-[#73C03F]">
                <div className='bg-[#73C03F]'>
                    <div className='flex justify-between '>
                        <p className='font-bold text-xl p-4 mt-4 text-white'>함께줍줍</p>
                        <div className='mr-4 mt-4 ml-auto flex items-center gap-2'>
                            <button
                                type='button'
                                onClick={() => navigate("/mypage")}
                            >
                                <img src='/account_circle.png' alt='회원' className='w-7'/>
                            </button>
                        </div>
                    </div>
                </div>

                {/* 상단 헤더 */}
                <div className="relative">
                    <img
                        src="image-2.png"
                        alt="hero"
                        className="w-full object-cover"
                    />
                    <img src='/hamkae-earth.png' alt='hamkae' className='absolute z-50 top-6 right-50 '/>
                    
                    {/* 인사 카드 */}
                    <div className="absolute -bottom-70 w-full flex justify-center">
                        <div className="bg-white rounded-t-3xl p-6 w-full max-w-lg">
                            <div className="flex justify-between items-center mb-4">
                                <div className="border-2 border-[#73C03F] rounded-2xl py-2 px-4 text-center font-semibold">
                                    반갑습니다. {displayName}님
                                </div>
                                {isAuthenticated && (
                                    <button
                                        onClick={handleLogout}
                                        className="bg-red-500 hover:bg-red-600 text-white px-3 py-2 rounded-lg text-sm font-medium transition-colors duration-200 flex items-center gap-1"
                                    >
                                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                                        </svg>
                                        로그아웃
                                    </button>
                                )}
                            </div>

                            <div className="flex justify-between items-center mb-3 mt-5">
                                <h2 className="font-semibold">근처 청소 할만한 곳</h2>
                            </div>

                            {/* 가로 스크롤 카드 */}
                            <div className="flex space-x-4 overflow-x-auto pb-4">
                                {places.map((place) => {
                                    const canClean = place.distance <= 15; // 10~15m 추천
                                    return (
                                        <div
                                            key={place.id}
                                            className="bg-white rounded-2xl shadow-md w-50 flex-shrink-0 flex flex-col"
                                        >
                                            <img
                                                src={place.imageUrl}
                                                alt={place.title}
                                                className="h-25 w-full object-cover rounded-t-2xl"
                                                onError={(e) => { e.currentTarget.src = '/fallback.jpg'; }}
                                            />
                                            <div className="p-3 flex-1">
                                                <h3 className="font-semibold text-sm truncate">
                                                    {place.title}
                                                </h3>
                                                <p className="text-xs font-bold mt-1">
                                                    {place.distance}m 거리
                                                </p>
                                            </div>
                                            <div className="bg-[#C8EEAF] px-3 py-2 rounded-b-2xl flex items-center justify-between">
                                                <span className="font-bold text-sm">{place.point}P</span>
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <Navbar />
        </div>
    );
};

export default Mainpage;