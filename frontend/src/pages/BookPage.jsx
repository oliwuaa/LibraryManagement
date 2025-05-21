import React, {useEffect, useState} from 'react';
import BookCard from '../components/BookCard';
import '../styles/BookPage.css';
import Navbar from '../components/Navbar.jsx';
import {fetchWithAuth} from '../Api.js';

const BooksPage = () => {
    const [libraries, setLibraries] = useState([]);
    const [selectedLibraryId, setSelectedLibraryId] = useState('all');
    const [copies, setCopies] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [userRole, setUserRole] = useState(null);
    const [isbnToAdd, setIsbnToAdd] = useState('');
    const [addBookError, setAddBookError] = useState(null);
    const [addBookSuccess, setAddBookSuccess] = useState(null);

    const token = localStorage.getItem('accessToken');

    useEffect(() => {
        const fetchUser = async () => {
            try {
                const res = await fetchWithAuth('/users/me');
                if (res.ok) {
                    const user = await res.json();
                    setUserRole(user.role);
                }
            } catch (e) {
                console.error(e);
            }
        };

        const fetchLibraries = async () => {
            try {
                const res = await fetchWithAuth('/libraries');
                if (res.ok) {
                    const data = await res.json();
                    setLibraries(data);
                }
            } catch (e) {
                console.error(e);
            }
        };

        fetchUser();
        fetchLibraries();
    }, [token]);

    useEffect(() => {
        if (!selectedLibraryId) return;

        const fetchCopiesOrBooks = async () => {
            setLoading(true);
            setError(null);
            try {
                if (selectedLibraryId === 'all') {
                    const res = await fetchWithAuth('/books');
                    if (!res.ok) throw new Error('Failed to fetch books');
                    const books = await res.json();
                    books.sort((a, b) => a.title.localeCompare(b.title));
                    const copiesLike = books.map(book => ({
                        book,
                        library: null,
                        status: 'AVAILABLE'
                    }));
                    setCopies(copiesLike);
                } else {
                    const res = await fetchWithAuth(`/copies/library/${selectedLibraryId}`);
                    if (!res.ok) throw new Error('Failed to fetch copies');
                    const data = await res.json();
                    data.sort((a, b) => {
                        if (a.status === 'AVAILABLE' && b.status !== 'AVAILABLE') return -1;
                        if (a.status !== 'AVAILABLE' && b.status === 'AVAILABLE') return 1;
                        return a.book.title.localeCompare(b.book.title);
                    });
                    setCopies(data);
                }
            } catch (e) {
                console.error(e);
                setError('Error fetching data.');
                setCopies([]);
            } finally {
                setLoading(false);
            }
        };

        fetchCopiesOrBooks();
    }, [selectedLibraryId]);

    const filteredBooksMap = new Map();

    copies.forEach(copy => {
        const bookId = copy.book.id;
        const existing = filteredBooksMap.get(bookId);

        if (!existing) {
            filteredBooksMap.set(bookId, {
                book: copy.book,
                isAvailable: copy.status === 'AVAILABLE',
            });
        } else if (copy.status === 'AVAILABLE') {
            filteredBooksMap.set(bookId, {
                ...existing,
                isAvailable: true,
            });
        }
    });

    const filteredBooks = Array.from(filteredBooksMap.values())
        .filter(({book}) => {
            const term = searchTerm.toLowerCase();
            return (
                book.title.toLowerCase().includes(term) ||
                book.author.toLowerCase().includes(term) ||
                (book.isbn && book.isbn.includes(term))
            );
        })
        .sort((a, b) => a.book.title.localeCompare(b.book.title));


    const handleAddBook = async () => {
        setAddBookError(null);
        setAddBookSuccess(null);

        if (!isbnToAdd.trim()) {
            setAddBookError('ISBN cannot be empty.');
            return;
        }

        try {
            const res = await fetchWithAuth(`/books/${isbnToAdd}`, {
                method: 'POST'
            });

            if (res.ok) {
                setAddBookSuccess('Book added successfully!');
                setIsbnToAdd('');
            } else {
                const errorData = await res.json();
                setAddBookError(errorData.error || 'Failed to add book.');
            }
        } catch (e) {
            setAddBookError('Network error while adding book.');
            console.error(e);
        }
    };


    return (
        <div className="books-layout">
            <Navbar/>
            <div className="filter-form">
                <label htmlFor="search-input">Choose library:</label>
                <select
                    id="library-select"
                    value={selectedLibraryId}
                    onChange={(e) => setSelectedLibraryId(e.target.value)}
                    className="search-input"
                >
                    <option value="all">All libraries</option>
                    {libraries.map((lib) => (
                        <option key={lib.id} value={lib.id}>
                            {lib.name}
                        </option>
                    ))}
                </select>
            </div>

            <div className="filter-form">
                <label htmlFor="search-input">Search books (title / author / ISBN):</label>
                <input
                    type="text"
                    id="search-input"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    placeholder="Enter title, author, or ISBN"
                    className="search-input"
                />
            </div>

            {(userRole === 'LIBRARIAN' || userRole === 'ADMIN') && (
                <div className="filter-form add-book-container">
                    <label htmlFor="isbn-input">Add book by ISBN:</label>
                    <div className="input-button-wrapper">
                        <input
                            id="isbn-input"
                            type="text"
                            placeholder="Enter ISBN"
                            value={isbnToAdd}
                            onChange={(e) => setIsbnToAdd(e.target.value)}
                            className="search-input"
                        />
                        <button onClick={handleAddBook} className="add-button">
                            Add
                        </button>
                    </div>
                    {addBookError && <p className="error-msg">{addBookError}</p>}
                    {addBookSuccess && <p className="success-msg">{addBookSuccess}</p>}
                </div>
            )}

            <div className="books-container">
                {loading ? (
                    <p>Loading books...</p>
                ) : error ? (
                    <p>{error}</p>
                ) : filteredBooks.length > 0 ? (
                    filteredBooks.map(({book, isAvailable}) => (
                        <div key={book.id} className={`book-card-wrapper ${isAvailable ? '' : 'unavailable'}`}>
                            <BookCard
                                id={book.id}
                                title={book.title}
                                author={book.author}
                                isbn={book.isbn}
                            />
                        </div>
                    ))
                ) : (
                    <p>No books found in this library.</p>
                )}
            </div>
        </div>
    );
};

export default BooksPage;
