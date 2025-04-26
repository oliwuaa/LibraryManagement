CREATE TABLE copies (
                        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        book_id BIGINT NOT NULL,
                        library_id BIGINT NOT NULL,
                        status VARCHAR(50) NOT NULL,
                        CONSTRAINT fk_copy_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
                        CONSTRAINT fk_copy_library FOREIGN KEY (library_id) REFERENCES libraries(id) ON DELETE CASCADE
);
