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
    ('hardpassword', 'user2@example.com', 'Emily', 'Jones', 'USER', NULL),
    ('qwerty123', 'michael.brown@example.com', 'Michael', 'Brown', 'USER', NULL),
    ('securepass', 'susan.white@example.com', 'Susan', 'White', 'LIBRARIAN', 2),
    ('letmein22', 'david.taylor@example.com', 'David', 'Taylor', 'USER', NULL),
    ('adminpass', 'lisa.moore@example.com', 'Lisa', 'Moore', 'LIBRARIAN', 1),
    ('test1234', 'robert.jackson@example.com', 'Robert', 'Jackson', 'USER', NULL),
    ('passw0rd', 'nancy.harris@example.com', 'Nancy', 'Harris', 'LIBRARIAN', 3),
    ('abc12345', 'charles.clark@example.com', 'Charles', 'Clark', 'USER', NULL),
    ('mypassword', 'karen.lewis@example.com', 'Karen', 'Lewis', 'USER', NULL),
    ('zxcvb678', 'daniel.walker@example.com', 'Daniel', 'Walker', 'LIBRARIAN', 2),
    ('sunshine', 'betty.hall@example.com', 'Betty', 'Hall', 'USER', NULL),
    ('superpass', 'thomas.allen@example.com', 'Thomas', 'Allen', 'LIBRARIAN', 3),
    ('testpass1', 'sarah.young@example.com', 'Sarah', 'Young', 'USER', NULL),
    ('h4ckmepls', 'mark.king@example.com', 'Mark', 'King', 'USER', NULL),
    ('pa55word', 'judy.wright@example.com', 'Judy', 'Wright', 'LIBRARIAN', 1),
    ('readbooks', 'kevin.scott@example.com', 'Kevin', 'Scott', 'USER', NULL),
    ('ilovebooks', 'barbara.green@example.com', 'Barbara', 'Green', 'LIBRARIAN', 2),
    ('letmeinplz', 'steven.adams@example.com', 'Steven', 'Adams', 'USER', NULL);