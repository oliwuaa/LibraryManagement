import React, {useEffect, useState} from 'react';
import Navbar from '../components/Navbar';
import {Link} from 'react-router-dom';
import '../styles/MyLibraryPage.css';
import {useNavigate} from 'react-router-dom';
import {fetchWithAuth} from '../Api.js';

const MyLibraryPage = () => {
    const [library, setLibrary] = useState(null);
    const [reservations, setReservations] = useState([]);
    const [loans, setLoans] = useState([]);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchLibrary = async () => {
            try {
                const res = await fetchWithAuth('/libraries/me');
                if (!res?.ok) throw new Error('Failed to fetch library');
                const data = await res.json();
                setLibrary(data);

                const resvRes = await fetchWithAuth(`/reservations/library/${data.id}`);
                if (resvRes?.status === 204) setReservations([]);
                else if (resvRes?.ok) setReservations(await resvRes.json());

                const loanRes = await fetchWithAuth(`/loans/library/${data.id}`);
                if (loanRes?.status === 204) setLoans([]);
                else if (loanRes?.ok) setLoans(await loanRes.json());
            } catch (err) {
                setError('Error while fetching data.');
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        fetchLibrary();
    }, []);

    const navigate = useNavigate();

    if (loading) return <p>Loading...</p>;
    if (error) return <p>{error}</p>;

    return (
        <div className="library-layout">
            <Navbar/>
            <div className="library-panel">
                <div className="library-info card-dark">
                    <h2>My Library</h2>
                    <p><strong>Name:</strong> {library.name}</p>
                    <p><strong>Address:</strong> {library.address}</p>
                    <Link to="/manage-resources">
                        <button className="dark-btn">Manage Resources</button>
                    </Link>
                </div>

                <div className="status-section card-dark">
                    <h3 className="section-header">Active Reservations</h3>
                    <div className="scrollable-section">
                        {reservations.filter(r => r.status === 'WAITING').length === 0 ? (
                            <p>No active reservations.</p>
                        ) : (
                            reservations.filter(r => r.status === 'WAITING').map(res => (
                                <div key={res.id} className="entry">
                                    <strong>Reservation #{res.id}</strong><br/>
                                    User ID: {res.userId}<br/>
                                    Copy ID: #{res.copyId}
                                </div>
                            ))
                        )}
                    </div>

                    <h3 className="section-header">Active Loans</h3>
                    <div className="scrollable-section">
                        {loans.filter(loan => !loan.returnDate).length === 0 ? (
                            <p>No active loans.</p>
                        ) : (
                            loans.filter(loan => !loan.returnDate).map(loan => (
                                <div key={loan.id} className="entry">
                                    <strong>Loan #{loan.id}</strong><br/>
                                    User ID: {loan.userID}<br/>
                                    Copy ID: #{loan.copyId}<br/>
                                    Period: {loan.startDate} – {loan.endDate}
                                </div>
                            ))
                        )}
                    </div>
                </div>
            </div>

            <div className="history-section card-dark">
                <h3 className="section-header clickable" onClick={() => navigate('/reservations')}>
                    Reservation History
                </h3>
                <div className="scrollable-section">
                    {reservations.length === 0 ? (
                        <p>No reservations found.</p>
                    ) : (
                        reservations.map(res => (
                            <div key={res.id} className="entry">
                                <strong>Reservation #{res.id}</strong><br/>
                                User ID: {res.userId}<br/>
                                Copy ID: #{res.copyId}<br/>
                                Status: {res.status}
                            </div>
                        ))
                    )}
                </div>

                <h3 className="section-header clickable" onClick={() => navigate('/loans')}>
                    Loan History
                </h3>

                <div className="scrollable-section">
                    {loans.length === 0 ? (
                        <p>No loans found.</p>
                    ) : (
                        loans.map(loan => (
                            <div key={loan.id} className="entry">
                                <strong>Loan #{loan.id}</strong><br/>
                                User ID: {loan.userID}<br/>
                                Copy ID: #{loan.copyId}<br/>
                                Period: {loan.startDate} – {loan.endDate}<br/>
                                {loan.returnDate && `Returned: ${loan.returnDate}`}
                            </div>
                        ))
                    )}
                </div>
            </div>
        </div>
    );
};

export default MyLibraryPage;