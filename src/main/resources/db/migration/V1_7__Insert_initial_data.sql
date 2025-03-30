-- Dodanie przykładowych bibliotek
INSERT INTO libraries (name, address, status)
VALUES
    ('Central Library', '123 Main St, City', 'ACTIVE'),
    ('Westside Library', '456 West St, City', 'ACTIVE'),
    ('Eastside Library', '789 East St, City', 'ACTIVE');

-- Dodanie przykładowych książek
INSERT INTO books (title, author, isbn)
VALUES
    ('The Great Gatsby', 'F. Scott Fitzgerald', '9780743273565'),
    ('1984', 'George Orwell', '9780451524935'),
    ('To Kill a Mockingbird', 'Harper Lee', '9780061120084');

-- Dodanie przykładowych egzemplarzy książek
INSERT INTO copies (book_id, library_id, status)
VALUES
    (1, 1, 'AVAILABLE'),
    (1, 2, 'BORROWED'),
    (2, 1, 'AVAILABLE'),
    (3, 3, 'RESERVED');

-- Dodanie przykładowych użytkowników
INSERT INTO users (password, email, name, surname, role, library_id)
VALUES
    ('password123', 'john.doe@example.com', 'John', 'Doe', 'USER', NULL),
    ('12345pas', 'librarian@example.com', 'Jane', 'Smith', 'LIBRARIAN', 1),
    ('hardpassword', 'user2@example.com', 'Emily', 'Jones', 'USER', NULL);

-- Dodanie przykładowych wypożyczeń
INSERT INTO loans (user_id, copy_id, start_date, end_date, return_date)
VALUES
    (1, 1, '2025-03-01', '2025-03-10', '2025-03-08'),
    (3, 2, '2025-03-05', '2025-03-15', NULL);

-- Dodanie przykładowych rezerwacji
INSERT INTO reservations (user_id, copy_id, created_at, expiration_date, status)
VALUES
    (1, 4, '2025-03-02 10:00:00', '2025-03-09 10:00:00', 'WAITING'),
    (2, 2, '2025-03-03 14:30:00', '2025-03-10 14:30:00', 'CANCELLED');
