import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import './index.css';
import "./css/style.css";
import { AuthProvider } from './lib/authContext.jsx';
import Mainpage from './pages/Mainpage';
import Mappage from './pages/Mappage';
import Reportpage from './pages/Reportpage';
import Uploadpage from './pages/Uploadpage';
import Loginpage from './pages/Loginpage';
import Errpage from './pages/Errpage';
import Introduce from './pages/Introduce';
import MyPage from './pages/Mypage';
import Register from './pages/Register';
import ReportHistory from './pages/ReportHistory';
import VerificationHistory from './pages/VerificationHistory';
import PointExchange from './pages/PointExchange';
import TestPage from './pages/TestPage';
import MyPins from './pages/MyPins';

function App() {
    return (
        <AuthProvider>
            <div className="App">
                <Routes>
                    {/* 테스트 페이지 - 문제 진단용 */}
                    <Route path="/test" element={<TestPage />} />
                    
                    {/* 기본 라우트 - 루트 경로를 메인페이지로 리다이렉트 */}
                    <Route path="/" element={<Navigate to="/home" replace />} />
                    
                    {/* 메인 페이지 */}
                    <Route path='/home' element={<Mainpage/>} />
                    
                    {/* 인증 관련 페이지 */}
                    <Route path='/login' element={<Loginpage/>} />
                    <Route path='/register' element={<Register/>} />
                    
                    {/* 메인 기능 페이지 */}
                    <Route path='/map' element={<Mappage/>} />
                    <Route path='/report' element={<Reportpage/>} />
                    <Route path='/upload/:markerId' element={<Uploadpage/>} />
                    
                    {/* 사용자 페이지 */}
                    <Route path='/mypage' element={<MyPage/>} />
                    <Route path='/MyPage' element={<MyPage/>} />
                    
                    {/* 기타 페이지 */}
                    <Route path='/introduce' element={<Introduce/>} />
                    <Route path='/Introduce' element={<Introduce/>} />
                    <Route path="/report-history" element={<ReportHistory />} />
                    <Route path="/verification-history" element={<VerificationHistory />} />
                    <Route path="/my-pins" element={<MyPins />} />
                    <Route path="/point-exchange" element={<PointExchange />} />
                    
                    {/* 에러 페이지 */}
                    <Route path='/err' element={<Errpage/>} />
                    
                    {/* 404 처리 - 존재하지 않는 경로는 메인페이지로 리다이렉트 */}
                    <Route path="*" element={<Navigate to="/home" replace />} />
                </Routes>
            </div>
        </AuthProvider>
    );
}

export default App;