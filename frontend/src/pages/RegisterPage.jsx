import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import '../styles/RegisterPage.css';
import '../App.css';

const API_URL = 'http://localhost:8080';

const RegisterPage = () => {
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [name, setName] = useState('');
    const [surname, setSurname] = useState('');
    const [message, setMessage] = useState('');

    const handleRegister = async () => {
        try {
            const res = await fetch(`${API_URL}/users/register`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({email, password, name, surname})
            });

            if (res.ok) {
                setMessage('Registration successful.');
                navigate('/login');
            } else {
                setMessage('Something went wrong');
            }
        } catch (err) {
            setMessage('Connection error');
        }
    };

    return (
        <div className="register-container">
            <h2>Registration</h2>
            <input type="text" placeholder="Name (optional)" value={name} onChange={(e) => setName(e.target.value)}/>
            <input type="text" placeholder="Surname (optional)" value={surname}
                   onChange={(e) => setSurname(e.target.value)}/>
            <input type="email" placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)}/>
            <input type="password" placeholder="Password" value={password}
                   onChange={(e) => setPassword(e.target.value)}/>
            <button onClick={handleRegister}>Register</button>
            {message && <p>{message}</p>}
            <p>Already have an account? <a href="#" onClick={() => navigate('/login')}>Log in</a></p>
        </div>
    );
};

export default RegisterPage;
