import React, { useEffect } from 'react';
import { useNavigate } from "react-router-dom";
import { useAuth } from '../lib/authContext.jsx';
import Login from '../components/Login';

const Loginpage = () => {
    const navigate = useNavigate();
    const { token, isAuthenticated } = useAuth();

    useEffect(() => {
        if (isAuthenticated) {
            navigate('/home');
        }
    }, [isAuthenticated, navigate]);

    const handleLoginSuccess = () => {
        navigate('/home');
    };

    return (
        <div>
            <Login onLoginSuccess={handleLoginSuccess} />
        </div>
    );
};

export default Loginpage;
