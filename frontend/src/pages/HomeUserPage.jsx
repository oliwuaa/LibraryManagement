import React, { useEffect, useState } from 'react';
import Navbar from '../components/Navbar';
import '../styles/HomeUserPage.css';
import Select from "react-select";
import { fetchWithAuth } from '../Api.js';

const UserProfilePage = () => {
    const [user, setUser] = useState(null);
    const [loans, setLoans] = useState(null);
    const [reservations, setReservations] = useState(null);
    const [searchTitle, setSearchTitle] = useState('');
    const [books, setBooks] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const userRes = await fetchWithAuth(`/users/me`);
                const userData = await userRes.json();
                setUser(userData);

                const booksPromise = fetchWithAuth(`/books`);
                let loansData = [];
                let reservationsData = [];


                try {
                    const loansRes = await fetchWithAuth(`/loans/user/${userData.id}`);
                    loansData = await loansRes.json();
                } catch (err) {
                    console.error('Failed to fetch loans:', err);
                    loansData = [];
                }

                try {
                    const reservationsRes = await fetchWithAuth(`/reservations/user/${userData.id}`);
                    reservationsData = await reservationsRes.json();
                } catch (err) {
                    console.error('Failed to fetch reservations:', err);
                    reservationsData = [];
                }

                const booksRes = await booksPromise;
                const booksData = await booksRes.json();

                setLoans(loansData ?? []);
                setReservations(reservationsData ?? []);
                setBooks(booksData ?? []);
            } catch (err) {
                console.error('Failed to load user or books:', err);
                setLoans([]);
                setReservations([]);
                setBooks([]);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    const handleCancelReservation = async (reservationId) => {
        try {
            await fetchWithAuth(`/reservations/${reservationId}/cancel`, {
                method: 'PUT',
                body: JSON.stringify({ status: 'CANCELLED' }),
            });
            setReservations(prev => prev.filter(r => r.id !== reservationId));
        } catch (err) {
            alert('Error cancelling reservation.');
            console.error(err);
        }
    };

    if (loading || loans === null || reservations === null) {
        return (
            <div className="user-profile-layout">
                <Navbar />
                <div className="user-profile-container">
                    <p>Loading your data...</p>
                </div>
            </div>
        );
    }

    const filteredLoans = loans.filter(l => l.title?.toLowerCase().includes(searchTitle.toLowerCase()));
    const filteredReservations = reservations.filter(r => r.title?.toLowerCase().includes(searchTitle.toLowerCase()));

    const bookOptions = [...books]
        .sort((a, b) => a.title.localeCompare(b.title))
        .map(b => ({
            value: b.title,
            label: b.title
        }));

    const customSelectStyles = {
        option: (base, state) => ({
            ...base,
            backgroundColor: state.isSelected
                ? '#4b5563'
                : state.isFocused
                    ? '#374151'
                    : '#1f2937',
            color: 'white',
            cursor: 'pointer',
        }),
        control: (base, state) => ({
            ...base,
            backgroundColor: '#374151',
            borderColor: '#4b5563',
            boxShadow: state.isFocused ? '0 0 0 1px #6b7280' : 'none',
            '&:hover': {
                borderColor: '#6b7280',
            },
        }),
        menu: (base) => ({
            ...base,
            backgroundColor: '#1f2937',
            color: 'white',
        }),
        singleValue: (base) => ({
            ...base,
            color: 'white',
        }),
        placeholder: (base) => ({
            ...base,
            color: '#9ca3af',
        }),
        input: (base) => ({
            ...base,
            color: 'white',
        }),
    };

    return (
        <div className="user-profile-layout">
            <Navbar />
            <div className="user-profile-container">
                <h2>Welcome, {user?.email} (#{user?.id})!</h2>
                {user && (
                    <div style={{ marginBottom: '1rem' }}>
                        <p><strong>Email:</strong> {user.email}</p>
                        <p><strong>Role:</strong> {user.role}</p>
                    </div>
                )}

                <br />
                <h3>Active Loans</h3>
                <div className="scrollable-section">
                    {loans.filter(l => !l.returnDate).length === 0 ? (
                        <p>No active loans.</p>
                    ) : (
                        <ul className="data-list">
                            {loans.filter(l => !l.returnDate).map(loan => (
                                <li key={loan.id}>
                                    <strong>{loan.title || 'Unknown title'}</strong><br />
                                    Loaned: {loan.startDate}<br />
                                    Due: {loan.endDate}
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
                <br />

                <h3>Active Reservations</h3>
                <div className="scrollable-section">
                    {reservations.filter(r => r.status === 'WAITING').length === 0 ? (
                        <p>No active reservations.</p>
                    ) : (
                        <ul className="data-list">
                            {reservations.filter(r => r.status === 'WAITING').map(res => (
                                <li key={res.id}>
                                    <strong>{res.title || 'Unknown title'}</strong><br />
                                    Reserved on: {res.createdAt}<br />
                                    Expires on: {res.expirationDate}

                                    <button onClick={() => handleCancelReservation(res.id)}>Cancel</button>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
                <br />
                <br />

                <h3>All Your Reservations & Loans</h3>
                <div className="filter-container">
                    <label htmlFor="book-select">Search by Title:</label>
                    <Select
                        options={bookOptions}
                        value={searchTitle ? { label: searchTitle, value: searchTitle } : null}
                        onChange={(selected) => setSearchTitle(selected ? selected.value : '')}
                        onInputChange={(inputValue) => setSearchTitle(inputValue)}
                        styles={customSelectStyles}
                        placeholder="Search title..."
                        isClearable
                    />
                </div>
                <br />
                <br />
                <h3>Reservations</h3>
                <div className="scrollable-section" style={{ maxHeight: '300px', marginBottom: '2rem' }}>
                    {filteredReservations.length === 0 ? (
                        <p>No matches found.</p>
                    ) : (
                        <ul className="data-list">
                            {filteredReservations.map(r => (
                                <li key={r.id}>
                                    <strong>{r.title || 'Unknown title'}</strong><br />
                                    Status: {r.status}<br />
                                    Reserved on: {new Date(r.createdAt).toLocaleDateString()}
                                </li>
                            ))}
                        </ul>
                    )}
                </div>

                <h3>Loans</h3>
                <div className="scrollable-section" style={{ maxHeight: '300px' }}>
                    {filteredLoans.length === 0 ? (
                        <p>No matches found.</p>
                    ) : (
                        <ul className="data-list">
                            {filteredLoans.map(l => (
                                <li key={l.id}>
                                    <strong>{l.title || 'Unknown title'}</strong><br />
                                    Loaned: {new Date(l.startDate).toLocaleDateString()}<br />
                                    Expires on: {new Date(l.endDate).toLocaleDateString()}<br />
                                    Returned: {l.returnDate ? new Date(l.returnDate).toLocaleDateString() : 'Not returned'}
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            </div>
        </div>
    );
};

export default UserProfilePage;
