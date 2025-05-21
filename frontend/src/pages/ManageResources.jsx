import React, {useEffect, useState, useRef} from 'react';
import {useParams} from 'react-router-dom';
import Navbar from '../components/Navbar';
import '../styles/ManageResources.css';
import {fetchWithAuth} from '../Api.js';

const ManageResourcesPage = () => {
    const [library, setLibrary] = useState(null);
    const [allBooks, setAllBooks] = useState([]);
    const [filteredBooks, setFilteredBooks] = useState([]);
    const [copiesForSelectedBook, setCopiesForSelectedBook] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [expandedBookId, setExpandedBookId] = useState(null);
    const [bookToAddCopy, setBookToAddCopy] = useState(null);
    const {libraryId} = useParams();

    const token = localStorage.getItem('accessToken');

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                const libUrl = libraryId ? `/libraries/${libraryId}` : `/libraries/me`;
                const libRes = await fetchWithAuth(libUrl);
                if (libRes.ok) {
                    const libData = await libRes.json();
                    setLibrary(libData);
                }

                const booksRes = await fetchWithAuth('/books');
                if (booksRes.status === 204) {
                    setAllBooks([]);
                    setFilteredBooks([]);
                } else if (booksRes.ok) {
                    const booksData = await booksRes.json();
                    booksData.sort((a, b) => a.title.localeCompare(b.title));
                    setAllBooks(booksData);
                    setFilteredBooks(booksData);
                }

                setLoading(false);
            } catch (err) {
                setError('Error fetching data.');
                setLoading(false);
                console.error(err);
            }
        };

        fetchData();
    }, [token, libraryId]);

    useEffect(() => {
        if (!searchTerm.trim()) {
            const sortedBooks = [...allBooks].sort((a, b) => a.title.localeCompare(b.title));
            setFilteredBooks(sortedBooks);
            return;
        }

        const search = searchTerm.toLowerCase();
        const results = allBooks.filter(
            (book) =>
                book.title.toLowerCase().includes(search) ||
                book.author.toLowerCase().includes(search) ||
                (book.isbn && book.isbn.includes(search))
        );
        setFilteredBooks(results);
    }, [searchTerm, allBooks]);

    const fetchCopiesByBook = async (bookId) => {
        if (!library) return;
        console.log('Fetching copies for book ID:', bookId, 'in library ID:', library.id);
        try {
            const res = await fetchWithAuth(`/copies/library/${library.id}/book/${bookId}`);
            console.log('Response status:', res.status);
            if (res.status === 204) {
                setCopiesForSelectedBook([]);
            } else if (res.ok) {
                const data = await res.json();
                console.log('Response data:', data);
                data.sort((a, b) => a.id - b.id);
                setCopiesForSelectedBook(data);
            }
        } catch (err) {
            console.error('Error fetching copies:', err);
            setCopiesForSelectedBook([]);
        }
    };

    const handleAddCopyToBook = (book) => {
        setBookToAddCopy(book);
    };

    const handleAddCopy = async (book) => {
        if (!book || !library) return;
        try {
            const res = await fetchWithAuth(`/copies/${book.id}/${library.id}`, {
                method: 'POST',
            });
            if (res.ok) {
                alert(`Copy added successfully to "${book.title}"!`);
                fetchCopiesByBook(book.id);
                if (expandedBookId === book.id) {
                    toggleBookCopies(book.id);
                    toggleBookCopies(book.id);
                }
            } else {
                alert('Error adding copy.');
            }
        } catch (err) {
            alert('Error adding copy.');
            console.error(err);
        }
    };

    const handleDeleteCopy = async (copyId, status) => {
        if (status !== 'AVAILABLE') {
            alert('Only copies with status AVAILABLE can be deleted.');
            return;
        }
        if (!window.confirm('Are you sure you want to delete this copy?')) return;
        try {
            const res = await fetchWithAuth(`/copies/${copyId}`, {
                method: 'DELETE',
            });
            if (res.ok) {
                alert('Copy deleted successfully.');
                if (expandedBookId) fetchCopiesByBook(expandedBookId);
            } else {
                alert('Error deleting copy.');
            }
        } catch (err) {
            alert('Error deleting copy.');
            console.error(err);
        }
    };

    const toggleBookCopies = (bookId) => {
        if (expandedBookId === bookId) {
            setExpandedBookId(null);
            setCopiesForSelectedBook([]);
            setBookToAddCopy(null);
        } else {
            setExpandedBookId(bookId);
            fetchCopiesByBook(bookId);
            setBookToAddCopy(null);
        }
    };

    return (
        <div className="manage-layout">
            <Navbar/>
            <div className="resource-container">
                <h2>Library: {library ? library.name : '...'}</h2>

                <div className="search-section">
                    <label htmlFor="book-search">Search books (title / author / ISBN):</label>
                    <input
                        type="text"
                        id="book-search"
                        placeholder="Enter title, author, or ISBN"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="search-input"
                    />
                </div>

                <div className="book-list-section">
                    <h3>Books in Library:</h3>
                    {filteredBooks.length === 0 ? (
                        <p>No books found.</p>
                    ) : (
                        <div className="book-list-grid">
                            {filteredBooks.map((book) => (
                                <div key={book.id} className="book-holder-wrapper"
                                     style={{display: 'flex', alignItems: 'center', justifyContent: 'space-between'}}>
                                    <div className="book-holder" style={{flexGrow: 1}}>
                                        <div className="book-header" onClick={() => toggleBookCopies(book.id)}
                                             style={{cursor: 'pointer'}}>
                                            <div className="book-info">
                                                <h4>{book.title}</h4>
                                                <p>{book.author}</p>
                                                <p className="book-isbn">ISBN: {book.isbn || 'N/A'}</p>
                                            </div>
                                            <div className="book-buttons" onClick={e => e.stopPropagation()}
                                                 style={{display: 'flex', gap: '4px'}}>
                                                <button className="add-copy-inline-btn"
                                                        onClick={() => handleAddCopy(book)}>
                                                    Add Copy
                                                </button>
                                                <button
                                                    className="expand-btn"
                                                    onClick={(e) => {
                                                        e.stopPropagation();
                                                        toggleBookCopies(book.id);
                                                    }}
                                                >
                                                    {expandedBookId === book.id ? '▲' : '▼'}
                                                </button>

                                            </div>
                                        </div>

                                        {expandedBookId === book.id && (
                                            <div className="book-expanded">
                                                <div className="scrollable-copies">
                                                    <h5>Copies:</h5>
                                                    {copiesForSelectedBook.length === 0 ? (
                                                        <p>No copies available for this book in this library.</p>
                                                    ) : (
                                                        <ul className="copies-list">
                                                            {copiesForSelectedBook.map((copy) => (
                                                                <li key={copy.id} className="copy-item">
                                                                    <span>Copy #{copy.id}</span>
                                                                    <span>Status: {copy.status}</span>
                                                                    {copy.status === 'AVAILABLE' && (
                                                                        <button
                                                                            className="delete-btn"
                                                                            onClick={() => handleDeleteCopy(copy.id, copy.status)}
                                                                        >
                                                                            Delete
                                                                        </button>
                                                                    )}
                                                                </li>
                                                            ))}
                                                        </ul>
                                                    )}
                                                </div>
                                            </div>
                                        )}
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

export default ManageResourcesPage;