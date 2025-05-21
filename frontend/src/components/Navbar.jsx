import React from 'react';
import { useNavigate, Link } from 'react-router-dom';
import '../styles/Navbar.css';
import '../App.css';

const Navbar = () => {
    const navigate = useNavigate();

    const handleLogout = () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('userRole');

        navigate('/');
    };

    const role = localStorage.getItem('userRole');

    return (
        <nav className="navbar">
            <div className="navbar-logo">
                <Link to="/">
                    <img src="/logo_flipped.png" alt="Library Logo" className="logo" />
                </Link>
            </div>
            <ul className="navbar-links">
                {role === 'LIBRARIAN' ? (
                    <li><Link to="/home-l">Home</Link></li>
                ) : role === 'ADMIN' ? (
                    <li><Link to="/home-a">Home</Link></li>
                ) : (
                    <li><Link to="/home-u">Home</Link></li>
                )}

                {role === 'LIBRARIAN' && (
                    <li><Link to="/profile">Profile</Link></li>
                )}
                {role === 'LIBRARIAN' && (
                    <li><Link to="/library">My Library</Link></li>
                )}
                <li><Link to="/books">Books</Link></li>
                {(role === 'ADMIN' || role === 'LIBRARIAN' ) && (
                    <li><Link to="/reservations">Reservations</Link></li>
                )}
                {(role === 'ADMIN' || role === 'LIBRARIAN' ) && (
                    <li><Link to="/loans">Loans</Link></li>
                )}
                {role === 'ADMIN' && (
                    <li><Link to="/users">Users</Link></li>
                )}
                <li>
                    <button onClick={handleLogout}>Logout</button>
                </li>
            </ul>
        </nav>
    );
};

export default Navbar;