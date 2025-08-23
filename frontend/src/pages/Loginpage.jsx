import React, { useEffect } from 'react';
import { useNavigate } from "react-router-dom";
import { useAuth } from '../lib/authContext.jsx';
import Login from '../components/Login';

const Loginpage = () => {
    const navigate = useNavigate();
    const { token, isAuthenticated } = useAuth();

    //김혜린 수정 2025-08-23
    //로그인 성공 시 소개글 페이지로 이동
    useEffect(() => {
        if (isAuthenticated) {
            // 로그인 상태면 소개글 페이지로 이동 (자동 스킵 로직은 Introduce.jsx에서 처리)
            navigate('/introduce');
        }
    }, [isAuthenticated, navigate]);

    const handleLoginSuccess = () => {
        // 로그인 성공 시 소개글 페이지로 이동
        navigate('/introduce');
    };

    return (
        <div>
            <Login onLoginSuccess={handleLoginSuccess} />
        </div>
    );
};

export default Loginpage;
