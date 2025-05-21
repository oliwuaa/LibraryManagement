import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/LoginPage.css';
import '../App.css';

const API_URL = 'http://localhost:8080';

const LoginPage = () => {
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [message, setMessage] = useState('');

    const handleLogin = async () => {
        try {
            const res = await fetch(`${API_URL}/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });

            if (!res.ok) {
                setMessage('Invalid login credentials');
                return;
            }

            const { accessToken, refreshToken, role } = await res.json();

            localStorage.setItem('accessToken', accessToken);
            localStorage.setItem('refreshToken', refreshToken);
            localStorage.setItem('userRole', role);

            setMessage(`Logged in as ${role}`);

            switch (role) {
                case 'LIBRARIAN':
                    navigate('/home-l');
                    break;
                case 'ADMIN':
                    navigate('/home-a');
                    break;
                default:
                    navigate('/home-u');
            }

        } catch (err) {
            console.error('Login error:', err);
            setMessage('Login error');
        }
    };


    return (
        <form
            className="login-container"
            onSubmit={(e) => {
                e.preventDefault();
                handleLogin();
            }}
        >
            <img src="/logo.png" alt="Library Logo" className="logo" />
            <input
                type="email"
                placeholder="Email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
            />
            <input
                type="password"
                placeholder="Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
            />
            <button type="submit">Log in</button>
            {message && <p>{message}</p>}
            <p>
                Don't have an account?{' '}
                <a href="#" onClick={() => navigate('/register')}>
                    Register
                </a>
            </p>
        </form>
    );
};

export default LoginPage;
