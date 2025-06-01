import React, {useEffect, useState} from 'react';
import {useParams} from 'react-router-dom';
import Navbar from '../components/Navbar';
import {fetchWithAuth} from '../Api.js';
import GlobalAlert from "../components/GlobalAlert.jsx";

const LibrariansManagePage = () => {
    const {libraryId} = useParams();
    const [libraryName, setLibraryName] = useState('');
    const [librarians, setLibrarians] = useState([]);
    const [searchEmail, setSearchEmail] = useState('');
    const [alertMsg, setAlertMsg] = useState('');
    const [alertType, setAlertType] = useState('info');

    useEffect(() => {
        const fetchLibrary = async () => {
            const res = await fetchWithAuth(`/libraries/${libraryId}`);
            if (res.ok) {
                const data = await res.json();
                setLibraryName(data.name);
            }
        };

        const fetchLibrarians = async () => {
            const res = await fetchWithAuth(`/users/library/${libraryId}/librarians`);
            if (res.ok) {
                const data = await res.json();
                setLibrarians(data);
            }
        };

        fetchLibrary();
        fetchLibrarians();
    }, [libraryId]);

    const filtered = librarians.filter(l =>
        l.email.toLowerCase().includes(searchEmail.toLowerCase())
    );

    const downgradeToUser = async (userId) => {
        setAlertType('');
        setAlertMsg('');
        try {
            const res = await fetchWithAuth(`/users/${userId}`, {
                method: 'PATCH',
                body: JSON.stringify('USER'),
            });

            if (res.ok) {
                setLibrarians(prev => prev.filter(u => u.id !== userId));
                setAlertType('success');
                setAlertMsg('Librarian downgraded successfully.');
            }
            else{
                setAlertType('error');
                setAlertMsg('Failed to downgrade user.');
            }
        } catch (err) {
            setAlertType('error');
            setAlertMsg('Error downgrading user.');
            console.error(err);
        }
    };

    return (
        <div className="manage-layout">
            <Navbar/>
            <GlobalAlert
                message={alertMsg}
                type={alertType}
                onClose={() => setAlertMsg('')}
            />
            <div className="resource-container">
                <h3>Librarians of "{libraryName}" (#{libraryId})</h3>

                <div className="search-section">
                    <label>Search by email:</label>
                    <input
                        type="text"
                        className="search-input"
                        value={searchEmail}
                        onChange={(e) => setSearchEmail(e.target.value)}
                        placeholder="Search email..."
                    />
                </div>

                <div className="book-list-section">
                    {filtered.length === 0 ? (
                        <p>No librarians found.</p>
                    ) : (
                        <div className="book-list-grid">
                            {filtered
                                .sort((a, b) => a.id - b.id)
                                .map(user => (
                                    <div key={user.id} className="book-holder">
                                        <div className="book-header" style={{cursor: 'default'}}>
                                            <div className="book-info">
                                                <h4>User #{user.id}</h4>
                                                <p><strong>Email:</strong> {user.email}</p>
                                                <p><strong>Status:</strong> LIBRARIAN</p>
                                            </div>
                                            <button className="delete-btn" onClick={() => downgradeToUser(user.id)}>
                                                Downgrade to USER
                                            </button>
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

export default LibrariansManagePage;
