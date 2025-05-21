import React from 'react';
import { Link } from 'react-router-dom';
import '../styles/BookCard.css';

const API_URL = 'http://localhost:8080';

const BookCard = ({ id, title, author, isbn, onDelete }) => {
    const imageUrl = isbn
        ? `https://covers.openlibrary.org/b/isbn/${isbn}-M.jpg`
        : '/placeholder.png';

    const role = localStorage.getItem('userRole');

    const handleDelete = async (e) => {
        e.preventDefault(); // zapobiega przeładowaniu z Link
        const token = localStorage.getItem('accessToken');
        if (!window.confirm(`Are you sure you want to delete "${title}"?`)) return;

        try {
            const res = await fetch(`${API_URL}/books/${id}`, {
                method: 'DELETE',
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (!res.ok) throw new Error('Failed to delete book');

            if (onDelete) onDelete(id);
        } catch (error) {
            console.error('Error deleting book:', error);
            alert('Failed to delete book.');
        }
    };

    return (
        <div className="book-card">
            {(role === 'LIBRARIAN' || role === 'ADMIN') && (
                <button className="delete-btn-top-right" onClick={handleDelete} title="Delete book">×</button>
            )}
            <Link to={`/book/${id}`} className="book-link">
                <img src={imageUrl} alt={`Cover of ${title}`} className="book-image" />
                <h3>{title}</h3>
                <p>{author}</p>
            </Link>
        </div>
    );
};

export default BookCard;
