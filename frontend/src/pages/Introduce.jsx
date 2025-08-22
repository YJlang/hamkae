import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const Introduce = () => {
    const navigate = useNavigate();

    const [showAIT, setShowAIT] = useState(false);
    const [checked, setChecked] = useState(false);
    const [currentPage, setCurrentPage] = useState(0);
    const [loading, setLoading] = useState(true);

    const pages = [
        {
            title: 'GUIDE 1',
            content: (
                <div className="flex flex-col items-center">
                    <p className="text-2xl font-black">
                        참여는 쉽게, 행동은 보이게
                    </p>
                    <img
                        src="/public/introduce1.png"
                        alt="소개1"
                        className="max-w-xs"
                    />
                </div>
            ),
        },
        {
            title: 'GUIDE 2',
            content: (
                <div className="flex flex-col items-center text-center">
                    <img
                        src="/public/introduce2.png"
                        alt="소개2"
                        className="max-w-xs mb-"
                    />
                    <div className="text-[15px] font-extrabold mb-7">
                        <div>
                            안양시 거리의 쓰레기 문제, 이제 함께 해결해요!
                        </div>
                    </div>
                    <div className="text-[15px] font-black leading-9 text-[#73C03F] whitespace-pre-line">
                        누구나 쉽게 제보하고,
                        <br />
                        직접 행동하면 보상이 따라옵니다.
                    </div>
                </div>
            ),
        },
        {
            title: 'GUIDE 3',
            content: (
                <div className="flex flex-col items-center text-center space-y-5 w-full">
                    {/* 카드 1 */}
                    <div className="flex flex-col items-center justify-center rounded-[30px] w-[294px] h-[140px] bg-white">
                        <img
                            src="/public/introduce3-1.png"
                            alt="제보하기"
                            className="w-[57px] h-[51px] mb-2"
                        />
                        <p className="text-sm font-extrabold text-black">
                            누구나 쉽게
                        </p>
                        <p className="text-xl font-extrabold text-[#73C03F]">
                            제보하기
                        </p>
                    </div>

                    {/* 카드 2 */}
                    <div className="flex flex-col items-center justify-center rounded-[30px] w-[294px] h-[140px] bg-white">
                        <img
                            src="/public/introduce3-2.png"
                            alt="참여하기"
                            className="w-[61px] h-[47px] mb-2"
                        />
                        <p className="text-sm font-extrabold text-black">
                            직접 행동하며
                        </p>
                        <p className="text-xl font-extrabold text-[#73C03F]">
                            참여하기
                        </p>
                    </div>

                    {/* 카드 3 */}
                    <div className="flex flex-col items-center justify-center rounded-[30px] w-[294px] h-[140px] bg-white">
                        <img
                            src="/public/introduce3-3.png"
                            alt="보상받기"
                            className="w-[37px] h-[61px] mb-2"
                        />
                        <p className="text-sm font-extrabold text-black">
                            참여하고
                        </p>
                        <p className="text-xl font-extrabold text-[#73C03F]">
                            보상받기
                        </p>
                    </div>
                </div>
            ),
        },
        {
            title: 'GUIDE 4',
            content: (
                <div className="flex flex-col items-center text-center">
                    <img
                        src="/public/introduce4.png"
                        alt="소개4"
                        className="max-w-xs mb-11"
                    />
                    <p className="text-lg font-black text-[#73C03F]">
                        작은 실천이 모여 큰 변화를 만듭니다!
                    </p>
                </div>
            ),
        },
        {
            title: 'GUIDE 5',
            content: (
                <div className="flex flex-col items-center text-center w-full">
                    <img
                        src="/public/introduce5.png"
                        alt="소개5"
                        className="max-w-xs"
                    />
                    <div className="flex items-center text-center space-x-1 mb-4 mt-6">
                        <img
                            src="/public/introduce5-1.png"
                            alt="체크표시"
                            className="w-[25px] h-[23px]"
                        />
                        <div className="text-[#FFE44A] font-black text-lg">
                            포인트는 이렇게 사용해요!
                        </div>
                    </div>
                    <p className="font-normal text-sm mb-6">
                        쓰레기 청소나 제보 활동을 하면 포인트가 쌓여요.
                        <br />
                        모은 포인트는 상품권으로 교환할 수 있어요.
                    </p>
                </div>
            ),
        },
        {
            title: 'GUIDE 6',
            content: (
                <div className="flex flex-col items-center text-center">
                    <img
                        src="/public/introduce6.png"
                        alt="소개6"
                        className="max-w-xs mb-4"
                    />
                    <p className="font-semibold text-xl">
                        지금 바로 함께줍줍 하러 가볼까요?
                    </p>
                </div>
            ),
        },
    ];

    // 로컬스토리지 확인
    useEffect(() => {
        const skip = localStorage.getItem('skipIntro');
        if (skip === 'true') {
            navigate('/map');
        } else {
            setLoading(false);
        }
    }, [navigate]);

    const handleNext = () => {
        if (currentPage < pages.length - 1) {
            setCurrentPage(currentPage + 1);
        } else {
            navigate('/home');
        }
    };

    if (loading) return <div>로딩 중...</div>;

    return (
        <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100 p-5 relative">
            <h3 className="mb-2 text-sm font-normal text-[#73C03F]">
                {pages[currentPage].title}
            </h3>

            <div className="flex mb-5">
                {pages.map((_, index) => (
                    <span
                        key={index}
                        className={`w-2 h-2 rounded-full mx-[3px] ${
                            index === currentPage
                                ? 'bg-[#73C03F]'
                                : 'bg-[#D9D9D9]'
                        }`}
                    ></span>
                ))}
            </div>

            <div className="flex-1 flex flex-col items-center justify-center w-full">
                {pages[currentPage].content}
            </div>

            {/* GUIDE 5 버튼 */}
            {currentPage === 4 && (
                <button
                    onClick={() => setShowAIT(true)}
                    className="bg-[#73C03F] hover:bg-green-500 text-white font-bold py-3 px-6 rounded-lg w-4/5 max-w-xs mt-6"
                >
                    완료
                </button>
            )}

            {/* 일반 다음 버튼 */}
            {currentPage !== 4 && (
                <button
                    onClick={handleNext}
                    className="bg-[#73C03F] hover:bg-green-500 text-white font-bold py-3 px-6 rounded-lg w-4/5 max-w-xs mt-6"
                >
                    {currentPage === pages.length - 1
                        ? '메인 화면으로 이동'
                        : '다음'}
                </button>
            )}

            {/* AIT 오버레이 */}
            {showAIT && (
                <div className="absolute inset-0 flex items-center justify-center z-50 bg-black/30">
                    <div className="bg-white p-5 rounded-lg text-center max-w-xs w-full">
                        <p className="mb-4 font-bold text-lg">
                            소개글을 보시겠습니까?
                        </p>
                        <label className="flex items-center space-x-2 mb-4">
                            <input
                                type="checkbox"
                                checked={checked}
                                onChange={(e) => setChecked(e.target.checked)}
                            />
                            <span>다시 보지 않기</span>
                        </label>
                        <button
                            onClick={() => {
                                if (checked) {
                                    localStorage.setItem('skipIntro', 'true');
                                }
                                setShowAIT(false);
                                setCurrentPage(5); // GUIDE 6로 이동
                            }}
                            className="bg-[#73C03F] hover:bg-green-500 text-white font-bold py-2 px-4 rounded-lg"
                        >
                            확인
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Introduce;
