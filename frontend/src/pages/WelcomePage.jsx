import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/WelcomePage.css';
import '../App.css';

const WelcomePage = () => {
    const navigate = useNavigate();

    return (
        <div className="home-container">
            <img src="/logo_full.png" alt="Library Logo" className="logo" />
            <button onClick={() => navigate('/login')}>Sign In</button>
            <p>Don't have account? <a href="#" onClick={() => navigate('/register')}>Sign up</a></p>
        </div>
    );
};

export default WelcomePage;
