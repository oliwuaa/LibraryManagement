-- Dodanie przykładowych wypożyczeń

INSERT INTO loans (user_id, copy_id, start_date, end_date, return_date)
SELECT
    (SELECT id FROM users WHERE email = 'michael.brown@example.com'),
    (SELECT id FROM copies WHERE status = 'BORROWED'),
    DATE '2025-03-05',
    DATE '2025-03-15',
    NULL;

-- Dodanie przykładowych rezerwacji

INSERT INTO reservations (user_id, copy_id, created_at, expiration_date, status)
SELECT
    (SELECT id FROM users WHERE email = 'john.doe@example.com'),
    (SELECT id FROM copies WHERE status = 'RESERVED'),
    TIMESTAMP '2025-03-02 10:00:00',
    TIMESTAMP '2025-03-09 10:00:00',
    'WAITING';
