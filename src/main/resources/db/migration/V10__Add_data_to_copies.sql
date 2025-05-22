-- Central Library: 30 kopii dla 10 książek
INSERT INTO copies (book_id, library_id, status)
SELECT b.id, l.id, 'AVAILABLE'
FROM books b, libraries l, generate_series(1, 30)
WHERE l.name = 'Central Library'
  AND b.title IN (
                  'Pride and Prejudice',
                  'Harry Potter and the Sorcerer''s Stone',
                  'The Hobbit',
                  'Brave New World',
                  'The Book Thief',
                  'The Lord of the Rings',
                  'The Da Vinci Code',
                  'A Game of Thrones',
                  'Little Women',
                  'Ender''s Game'
    );

-- Westside Library: 25 kopii dla 10 książek
INSERT INTO copies (book_id, library_id, status)
SELECT b.id, l.id, 'AVAILABLE'
FROM books b, libraries l, generate_series(1, 25)
WHERE l.name = 'Westside Library'
  AND b.title IN (
                  'The Catcher in the Rye',
                  'Fahrenheit 451',
                  'Moby Dick',
                  'Animal Farm',
                  'The Picture of Dorian Gray',
                  'Les Misérables',
                  'The Giver',
                  'The Little Prince',
                  'Slaughterhouse-Five',
                  'Mistborn'
    );

-- Eastside Library: 20 kopii dla 10 książek
INSERT INTO copies (book_id, library_id, status)
SELECT b.id, l.id, 'AVAILABLE'
FROM books b, libraries l, generate_series(1, 20)
WHERE l.name = 'Eastside Library'
  AND b.title IN (
                  'War and Peace',
                  'The Odyssey',
                  'Crime and Punishment',
                  'Jane Eyre',
                  'Wuthering Heights',
                  'The Help',
                  'Divergent',
                  'Dracula',
                  'Coraline',
                  'The Martian'
    );
