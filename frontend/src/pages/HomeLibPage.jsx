import React, {useEffect, useState} from 'react';
import Navbar from '../components/Navbar';
import Select from 'react-select';
import '../styles/HomeLibPage.css';
import {fetchWithAuth} from '../Api.js';


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
    control: (base) => ({
        ...base,
        backgroundColor: '#374151',
        borderColor: '#4b5563',
        color: 'white',
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

const HomeLibPage = () => {
    const [reservations, setReservations] = useState([]);
    const [users, setUsers] = useState([]);
    const [searchUser, setSearchUser] = useState('');
    const [availableCopies, setAvailableCopies] = useState([]);
    const [selectedUser, setSelectedUser] = useState('');
    const [selectedCopy, setSelectedCopy] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const token = localStorage.getItem('accessToken');

    useEffect(() => {
        const fetchData = async () => {
            try {
                const libRes = await fetchWithAuth(`/libraries/me`);
                if (!libRes.ok) throw new Error("Failed to fetch library info");
                const library = await libRes.json();

                const [res, usersRes, copiesRes] = await Promise.all([
                    fetchWithAuth(`/reservations/library/${library.id}`),
                    fetchWithAuth(`/users/search?role=USER`),
                    fetchWithAuth(`/copies/library/${library.id}/available`)
                ]);

                if ((!res.ok && res.status !== 204) || !usersRes.ok || !copiesRes.ok) {
                    throw new Error("Failed to fetch data");
                }

                const reservationsData = (res.status === 204) ? [] : await res.json();
                const usersData = (usersRes.status === 204) ? [] : await usersRes.json();
                const copiesData = (copiesRes.status === 204) ? [] : await copiesRes.json();

                setReservations(reservationsData.filter(r => r.status === 'WAITING'));
                setUsers(usersData.sort((a, b) => {
                    const emailA = a.email || '';
                    const emailB = b.email || '';
                    return emailA.localeCompare(emailB);
                }));
                setAvailableCopies(copiesData.sort((a, b) => {
                    const titleA = a.book?.title || '';
                    const titleB = b.book?.title || '';
                    return titleA.localeCompare(titleB);
                }));

            } catch (err) {
                setError('Failed to load data.');
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);


    const filteredUsers = users.filter(u =>
        u.email?.toLowerCase().includes(searchUser.toLowerCase())
    );

    const handleApprove = async (reservation) => {
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

    const handleReject = async (id) => {
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

    const handleManualLoan = async () => {
        if (!selectedUser || !selectedCopy) {
            alert('Please select both a user and a copy.');
            return;
        }

        try {
            const res = await fetchWithAuth(`/loans?userId=${selectedUser}&copyId=${selectedCopy}`, {
                method: 'POST'
            });

            if (!res.ok) throw new Error("Loan creation failed");

            alert('Loan created successfully!');
            setSelectedUser('');
            setSelectedCopy('');
        } catch (err) {
            alert('Error while creating the loan.');
            console.error(err);
        }
    };

    if (loading) return <p>Loading...</p>;
    if (error) return <p style={{color: 'red'}}>{error}</p>;

    return (
        <div className="handle-layout">
            <Navbar/>
            <div className="handle-container">
                <h3>Manual Loan Creation</h3>
                <div className="manual-loan-form">

                    {filteredUsers.length === 0 ? (
                        <p style={{color: 'gray'}}>No matching users.</p>
                    ) : (
                        <div style={{marginBottom: '1rem'}}>
                            <label>Select User:</label>
                            <Select
                                options={users.map(user => ({
                                    value: user.id,
                                    label: `${user.email} (#${user.id})`
                                }))}
                                onChange={(selectedOption) => setSelectedUser(selectedOption?.value || '')}
                                value={
                                    selectedUser
                                        ? {
                                            value: selectedUser,
                                            label: users.find(u => u.id === selectedUser)?.email + ` (#${selectedUser})`
                                        }
                                        : null
                                }
                                placeholder="Start typing to search..."
                                isClearable
                                styles={customSelectStyles}
                            />
                        </div>

                    )}

                    {availableCopies.length === 0 ? (
                        <p style={{color: 'gray'}}>No available copies in your library.</p>
                    ) : (
                        <div style={{marginBottom: '1rem'}}>
                            <label>Select Available Copy:</label>
                            <Select
                                options={availableCopies.map(copy => ({
                                    value: copy.id,
                                    label: `${copy.book.title} – #${copy.id}`
                                }))}
                                onChange={(selectedOption) => setSelectedCopy(selectedOption?.value || '')}
                                value={
                                    selectedCopy
                                        ? {
                                            value: selectedCopy,
                                            label:
                                                availableCopies.find(c => c.id === selectedCopy)?.book?.title +
                                                ` – #${selectedCopy}`
                                        }
                                        : null
                                }
                                placeholder="Search copy by book title..."
                                isClearable
                                styles={customSelectStyles}
                            />
                        </div>
                    )}

                    <button
                        className="create-btn"
                        onClick={handleManualLoan}
                        disabled={!selectedUser || !selectedCopy}
                    >
                        Create Loan
                    </button>
                </div>

                <br></br>
                <br></br>

                <h3>Pending Reservations</h3>
                {reservations.length === 0 ? (
                    <p>No reservations awaiting approval.</p>
                ) : (
                    <div className="reservation-scroll">
                        {reservations.map(res => (
                            <div key={res.id} className="reservation-card">
                                <p>
                                    <strong>Reservation #{res.id}</strong><br/>
                                    User (#{res.userId}) : {res.email} <br/>
                                    Copy (#{res.copyId}) : {res.title}
                                </p>
                                <div className="button-group">
                                    <button className="approve-btn" onClick={() => handleApprove(res)}> Approve</button>
                                    <button className="reject-btn" onClick={() => handleReject(res.id)}> Reject</button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default HomeLibPage;
