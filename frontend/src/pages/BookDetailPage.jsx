import React, {useState, useEffect} from 'react';
import {useParams} from 'react-router-dom';
import Navbar from '../components/Navbar';
import '../styles/BookDetailPage.css';
import {fetchWithAuth} from '../Api.js';
import GlobalAlert from '../components/GlobalAlert';

const OPEN_LIBRARY_API_URL = 'https://openlibrary.org/api/books?bibkeys';

const BookDetailPage = () => {
    const {bookId} = useParams();
    const [book, setBook] = useState(null);
    const [copies, setCopies] = useState([]);
    const [selectedLibraryId, setSelectedLibraryId] = useState(null);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);
    const [userRole, setUserRole] = useState(null);
    const [openLibraryData, setOpenLibraryData] = useState(null);
    const [alertMsg, setAlertMsg] = useState('');
    const [alertType, setAlertType] = useState('info');

    useEffect(() => {
        const fetchUserInfo = async () => {
            try {
                const response = await fetchWithAuth('/users/me');

                if (response.ok) {
                    const data = await response.json();
                    setUserRole(data);
                } else {
                    throw new Error('Error fetching user data');
                }
            } catch (err) {
                setError(err.message);
            }
        };

        fetchUserInfo();
    }, []);

    useEffect(() => {
        const fetchBookDetails = async () => {
            setLoading(true);
            try {
                const response = await fetchWithAuth(`/books/${bookId}`);

                if (!response.ok) throw new Error('Error fetching book data');
                const bookData = await response.json();
                setBook(bookData);

                if (bookData.isbn) {
                    const openLibraryResponse = await fetch(
                        `${OPEN_LIBRARY_API_URL}=ISBN:${bookData.isbn}&format=json&jscmd=data`
                    );
                    if (openLibraryResponse.ok) {
                        const openLibraryInfo = await openLibraryResponse.json();
                        setOpenLibraryData(openLibraryInfo[`ISBN:${bookData.isbn}`]);
                    }
                }

                const copiesResponse = await fetchWithAuth(`/copies/book/${bookId}/available`);

                if (copiesResponse.ok) {
                    const copiesData = copiesResponse.status === 204 ? [] : await copiesResponse.json();
                    setCopies(copiesData);
                } else {
                    throw new Error('Error fetching copies');
                }
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchBookDetails();
    }, [bookId]);

    const handleReserve = async () => {
        setAlertType('');
        setAlertMsg('');
        if (!selectedLibraryId) {
            setAlertType('warning');
            setAlertMsg('Select a library first.');
            return;
        }

        const eligibleCopies = copies.filter(copy => copy.library.id === parseInt(selectedLibraryId));
        if (eligibleCopies.length === 0) {
            setAlertType('info');
            setAlertMsg('No available copies in this library.');
            return;
        }

        const randomCopy = eligibleCopies[Math.floor(Math.random() * eligibleCopies.length)];

        try {
            const response = await fetchWithAuth(`/reservations?copyId=${randomCopy.id}`, {
                method: 'POST'
            });

            if (response.ok) {
                setAlertType('success');
                setAlertMsg('Book reserved successfully!');
            } else {
                const errMsg = await response.text();
                setAlertType('error');
                setAlertMsg(`Reservation failed: ${errMsg || 'Server error.'}`);
            }
        } catch (error) {
            setAlertType('error');
            setAlertMsg('Unexpected error occurred.');
            console.error(error);
        }
    };

    const imageUrl = book?.isbn
        ? `https://covers.openlibrary.org/b/isbn/${book.isbn}-M.jpg`
        : '/placeholder.png';

    const librariesWithCopies = copies.reduce((acc, copy) => {
        const libId = copy.library.id;
        if (!acc[libId]) {
            acc[libId] = {
                library: copy.library,
                copies: [],
            };
        }
        acc[libId].copies.push(copy);
        return acc;
    }, {});

    if (loading) return <p>Loading...</p>;
    if (error) return <p>{error}</p>;

    return (
        <div>
            <Navbar/>
            <GlobalAlert
                message={alertMsg}
                type={alertType}
                onClose={() => setAlertMsg('')}
            />

            <div className="book-detail">
                <div className="detail-card">
                    <div className="book-image-container">
                        <img src={imageUrl} alt={book.title} className="book-cover"/>
                    </div>
                    <div className="book-detail-info">
                        <h2>{book.title}</h2>
                        <p><strong>Author:</strong> {book.author}</p>
                        <p><strong>ISBN:</strong> {book.isbn}</p>
                        {openLibraryData?.number_of_pages && (
                            <p><strong>Pages:</strong> {openLibraryData.number_of_pages}</p>
                        )}
                    </div>
                </div>

                <div className="library-card">
                    <h3>Available in :</h3>

                    {Object.values(librariesWithCopies).length === 0 ? (
                        <p>No available copies</p>
                    ) : (
                        <>
                            <ul className="library-list">
                                {Object.values(librariesWithCopies).map(({library, copies}) => (
                                    <li key={library.id}>
                                        <label>
                                            {userRole?.role === 'USER' && (
                                                <input
                                                    type="radio"
                                                    name="library"
                                                    value={library.id}
                                                    checked={selectedLibraryId === String(library.id)}
                                                    onChange={(e) => {
                                                        const value = e.target.value;
                                                        setSelectedLibraryId(prev => (prev === value ? null : value));
                                                    }}
                                                />

                                            )}
                                            {library.name} ({copies.length})
                                        </label>
                                    </li>
                                ))}
                            </ul>

                            {userRole?.role === 'USER' && selectedLibraryId && (
                                <button className="reserve-button" onClick={handleReserve}>
                                    Reserve
                                </button>
                            )}
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default BookDetailPage;
