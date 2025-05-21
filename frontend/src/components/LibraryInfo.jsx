import React, { useEffect, useState } from 'react';
import '../styles/LibraryInfo.css';

const API_URL = 'http://localhost:8080';

const LibraryDetails = ({ libraryId }) => {
    const [library, setLibrary] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchLibrary = async () => {
            setLoading(true);
            try {
                const response = await fetch(`${API_URL}/libraries/${libraryId}`, {
                    headers: {
                        Authorization: `Bearer ${localStorage.getItem('accessToken')}`,
                    },
                });

                if (response.ok) {
                    const data = await response.json();
                    setLibrary(data);
                } else if (response.status === 404) {
                    setLibrary(null);
                    setError('Library not found.');
                } else {
                    throw new Error('Failed to fetch library');
                }
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        if (libraryId) {
            fetchLibrary();
        }
    }, [libraryId]);

    return (
        <div className="library-container">
            <h2 className="library-header">Library Details</h2>
            {loading ? (
                <p>Loading...</p>
            ) : error ? (
                <p style={{ color: 'red' }}>{error}</p>
            ) : !library ? (
                <p>No library data available.</p>
            ) : (
                <table className="library-table">
                    <tbody>
                    <tr>
                        <th>Name</th>
                        <td>{library.name}</td>
                    </tr>
                    <tr>
                        <th>Address</th>
                        <td>{library.address}</td>
                    </tr>
                    </tbody>
                </table>
            )}
        </div>
    );
};

export default LibraryDetails;
