// [포인트 전환]
import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from '../lib/authContext.jsx';
import { pointHistoryAPI } from '../lib/pointHistoryAPI';
import Navbar from '../components/Navbar';

const PointExchange = () => {
  const navigate = useNavigate();
  const { username } = useAuth();
  
  // 상태 변수들
  const [currentPoints, setCurrentPoints] = useState(0);
  const [availablePoints, setAvailablePoints] = useState(0);
  const [exchangeAmount, setExchangeAmount] = useState('');
  const [exchangeHistory, setExchangeHistory] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [pointHistory, setPointHistory] = useState([]);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [summary, setSummary] = useState({ currentPoints: 0, availablePoints: 0 });
  
  const itemsPerPage = 6; // 페이지당 아이템 수
    const token = localStorage.getItem("token"); // JWT 저장값 사용

    // 포인트 현황 + 이력 불러오기
    useEffect(() => {
        const fetchData = async () => {
            try {
                // 포인트 요약 정보 가져오기
                const summaryRes = await pointHistoryAPI.getSummary();
                if (summaryRes?.data) {
                    setSummary(summaryRes.data);
                }
                
                // 포인트 이력 가져오기
                const historyRes = await pointHistoryAPI.getHistory();
                const list = (historyRes?.data || historyRes || []);
                setPointHistory(list);
                setTotalPages(Math.ceil(list.length / itemsPerPage));
            } catch (err) {
                console.error("포인트 데이터 불러오기 실패:", err);
            }
        };
        fetchData();
    }, []);

    const paginatedHistory = pointHistory.slice(
        (page - 1) * itemsPerPage,
        page * itemsPerPage
    );

    // 포인트 전환하기 (상품권 교환 요청)
    const handleExchange = async () => {
        try {
            const res = await pointHistoryAPI.redeem({ pointsUsed: 5000, rewardType: "FIVE_THOUSAND" });
            if (res.success ?? true) {
                alert("상품권 교환 완료! 핀번호가 발급되었습니다.");
                window.location.reload();
            } else {
                alert(res.message || "교환에 실패했습니다.");
            }
        } catch (err) {
            console.error("상품권 교환 실패:", err);
            alert("상품권 교환 중 오류가 발생했습니다.");
        }
    };

    return (
        <div className="w-[375px] mx-auto bg-white min-h-screen font-sans flex flex-col">
            {/* ===== 상단 헤더 ===== */}
            <div className="bg-[#73C03F] text-white relative pt-1.5 pb-1.5 px-1.5">
                {/* 뒤로가기 */}
                <img
                    src="../../public/arrow.png"
                    alt="뒤로가기"
                    className="w-6 h-6 absolute top-4 left-4"
                    onClick={() => navigate("/mypage")}
                />
                {/* 이름 */}
                <div className="flex items-center justify-end mt-8 pr-4">
                    <div className="text-xl font-semibold">{username || '사용자'} 님</div>
                </div>
                {/* 흰색 포인트 박스 */}
                <div className="bg-white text-[#73C03F] rounded-t-[20px] mt-4 py-6 px-5 flex flex-col items-center">
                    <img
                        src="../../public/logo.svg"
                        alt="profile"
                        className="w-16 h-16 absolute top-10 left-12"
                    />
                    <div className="w-full flex justify-around font-bold mb-4">
                        <div className="text-center">
                            <div className="text-sm">보유 포인트</div>
                            <div className="text-4xl">{summary.currentPoints}P</div>
                        </div>
                        <div className="flex items-center text-lg">▶</div>
                        <div className="text-center">
                            <div className="text-sm">전환 가능</div>
                            <div className="text-4xl">{summary.availablePoints}P</div>
                        </div>
                    </div>

                    <button
                        onClick={handleExchange}
                        className="bg-[#73C03F] text-white font-bold rounded-lg px-6 py-2 mt-1 shadow"
                    >
                        전환하기
                    </button>
                </div>
            </div>

            {/* ===== 전환 내역 영역 ===== */}
            <div className="flex-1 mt-3">
                {/* 헤더 */}
                <div className="grid grid-cols-3 text-[#73C03F] font-semibold text-xs py-3 px-3 text-center border-b border-[#73C03F]">
                    <div>전환날짜</div> {/* 날짜 */}
                    <div>전환포인트</div> {/* 포인트 */}
                    <div>포인트상태</div> {/* 타입 */}
                </div>

                {/* 데이터 행 */}
                {paginatedHistory.map((item) => (
                    <div
                        key={item.id}
                        className="grid grid-cols-3 text-center text-sm py-3 px-3 border-b border-[#73C03F]"
                    >
                        <div className="text-[#73C03F]">
                            {new Date(item.createdAt).toLocaleDateString()}
                        </div>
                        <div className="text-[#73C03F]">{item.points}P</div>
                        <div className="text-[#73C03F]">{item.type === "EARNED" ? "적립" : "사용"}</div>
                    </div>
                ))}
            </div>

            {/* ===== 페이지네이션 ===== */}
            <div className="mt-2">
                <div className="flex justify-center items-center space-x-4 py-4">
                    <button
                        disabled={page === 1}
                        onClick={() => setPage((p) => Math.max(p - 1, 1))}
                        className={`text-[#73C03F] text-lg ${page === 1 ? "opacity-30 cursor-default" : "cursor-pointer"}`}
                    >
                        ◀
                    </button>

                    <span className="text-sm text-[#73C03F] font-semibold">{page}</span>

                    <button
                        disabled={page === totalPages}
                        onClick={() => setPage((p) => Math.min(p + 1, totalPages))}
                        className={`text-[#73C03F] text-lg ${page === totalPages ? "opacity-30 cursor-default" : "cursor-pointer"}`}
                    >
                        ▶
                    </button>
                </div>
            </div>
            <Navbar />
        </div>
    );
};

export default PointExchange;
