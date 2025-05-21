import React, {useEffect, useState} from 'react';
import Select from 'react-select';
import Navbar from '../components/Navbar';
import '../styles/ManageResources.css';
import '../styles/ReservationPage.css';
import {fetchWithAuth} from '../Api.js';

const ReservationsPage = () => {
    const [reservations, setReservations] = useState([]);
    const [filtered, setFiltered] = useState([]);
    const [statusFilter, setStatusFilter] = useState('ALL');
    const [searchTitle, setSearchTitle] = useState('');
    const [searchEmail, setSearchEmail] = useState('');
    const [userRole, setUserRole] = useState(null);
    const [books, setBooks] = useState([]);
    const [users, setUsers] = useState([]);

    const token = localStorage.getItem('accessToken');

    useEffect(() => {
        const fetchUserInfo = async () => {
            try {
                const res = await fetchWithAuth('/users/me');
                if (!res.ok) throw new Error('Failed to fetch user info');
                const data = await res.json();
                setUserRole(data.role);

                let reservationsRes;
                if (data.role === 'ADMIN') {
                    reservationsRes = await fetchWithAuth('/reservations');
                } else {
                    const libraryRes = await fetchWithAuth('/libraries/me');
                    if (!libraryRes.ok) throw new Error('Failed to fetch library');
                    const libData = await libraryRes.json();

                    reservationsRes = await fetchWithAuth(`/reservations/library/${libData.id}`);
                }

                if (reservationsRes.ok) {
                    const result = await reservationsRes.json();
                    setReservations(result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)));
                } else {
                    setReservations([]);
                }

                const booksRes = await fetchWithAuth('/books');
                if (!booksRes.ok) throw new Error('Failed to fetch books');
                const booksData = await booksRes.json();
                setBooks(booksData);

                const usersRes = await fetchWithAuth('/users/search?role=USER');
                if (!usersRes.ok) throw new Error('Failed to fetch users');
                const usersData = await usersRes.json();
                setUsers(usersData);

            } catch (err) {
                console.error(err);
                setError('Error fetching data.');
            }
        };

        fetchUserInfo();
    }, []);


    useEffect(() => {
        let result = [...reservations];

        if (statusFilter !== 'ALL') {
            result = result.filter(r => r.status === statusFilter);
        }

        if (searchTitle.trim()) {
            result = result.filter(r => (r.title || '').toLowerCase().includes(searchTitle.toLowerCase()));
        }

        if (searchEmail.trim()) {
            result = result.filter(r => (r.email || '').toLowerCase().includes(searchEmail.toLowerCase()));
        }

        setFiltered(result);
    }, [statusFilter, searchTitle, searchEmail, reservations]);

    const cancelReservation = async (id) => {
        try {
            const res = await fetchWithAuth(`/reservations/${id}/cancel`, {method: 'PUT'});
            if (res.ok) {
                setReservations(prev => prev.map(r => r.id === id ? {...r, status: 'CANCELLED'} : r));
            } else {
                alert('Failed to cancel reservation.');
            }
        } catch (err) {
            console.error(err);
            alert('Error cancelling reservation.');
        }
    };

    const acceptReservation = async (reservation) => {
        try {
            const loanRes = await fetchWithAuth(`/loans?userId=${reservation.userId}&copyId=${reservation.copyId}`, {
                method: 'POST'
            });
            if (!loanRes.ok) throw new Error("Loan creation failed");

            setReservations(prev => prev.filter(r => r.id !== reservation.id));
        } catch (err) {
            console.error(err);
            alert('Error while approving reservation.');
        }
    };

    const bookOptions = [...books]
        .sort((a, b) => a.title.localeCompare(b.title))
        .map(b => ({
            value: b.title,
            label: b.title
        }));

    const userOptions = [...users]
        .sort((a, b) => a.email.localeCompare(b.email))
        .map(u => ({
            value: u.email,
            label: u.email
        }));


    const statusOptions = [
        {value: 'ALL', label: 'All'},
        {value: 'WAITING', label: 'Waiting'},
        {value: 'CANCELLED', label: 'Cancelled'},
        {value: 'EXPIRED', label: 'Expired'},
        {value: 'REALIZED', label: 'Realized'},
    ];

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
        <div className="manage-layout">
            <Navbar/>
            <div className="resource-container">
                <h3>Reservations</h3>

                <div className="search-section">
                    <div className="filter-container">
                        <label htmlFor="status-select">Status:</label>
                        <div className="custom-select-wrapper">
                            <Select
                                options={statusOptions}
                                value={statusOptions.find(option => option.value === statusFilter)}
                                onChange={(selected) => setStatusFilter(selected.value)}
                                styles={customSelectStyles}
                                placeholder="Select status..."
                                isSearchable={false}
                            />
                        </div>
                    </div>

                    <div className="filter-container">
                        <label htmlFor="book-select">Search by Title:</label>
                        <Select
                            options={bookOptions}
                            value={bookOptions.find(b => b.value === searchTitle) || null}
                            onChange={(selected) => setSearchTitle(selected ? selected.value : '')}
                            styles={customSelectStyles}
                            placeholder="Search title..."
                            isClearable
                        />
                    </div>

                    <div className="filter-container">
                        <label htmlFor="user-select">Search by User Email:</label>
                        <Select
                            options={userOptions}
                            value={userOptions.find(u => u.value === searchEmail) || null}
                            onChange={(selected) => setSearchEmail(selected ? selected.value : '')}
                            styles={customSelectStyles}
                            placeholder="Search email..."
                            isClearable
                        />
                    </div>
                </div>

                <div className="book-list-section">
                    <h3></h3>
                    {filtered.length === 0 ? (
                        <p>No reservations found.</p>
                    ) : (
                        <div className="book-list-grid">
                            {filtered.map((r) => (
                                <div key={r.id} className="book-holder-wrapper">
                                    <div className="book-holder">
                                        <div className="book-header" style={{cursor: 'default'}}>
                                            <div className="book-info">
                                                <h4>Reservation #{r.id}</h4>
                                                <p><strong>User:</strong> {r.email}</p>
                                                <p><strong>Copy:</strong> {r.title} (#{r.copyId})</p>
                                                <p>
                                                    <strong>Status:</strong>{' '}
                                                    <span
                                                        className={`status-label status-${r.status.toLowerCase()}`}>{r.status}</span>
                                                </p>
                                                <p><strong>Created:</strong> {new Date(r.createdAt).toLocaleString()}
                                                </p>
                                                <p>
                                                    <strong>Expires:</strong> {new Date(r.expirationDate).toLocaleString()}
                                                </p>
                                            </div>
                                            {r.status === 'WAITING' && (
                                                <div className="book-buttons">
                                                    <button className="add-copy-inline-btn"
                                                            onClick={() => acceptReservation(r)}>Accept
                                                    </button>
                                                    <button className="delete-btn"
                                                            onClick={() => cancelReservation(r.id)}>Cancel
                                                    </button>
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ReservationsPage;
