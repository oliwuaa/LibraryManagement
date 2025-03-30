CREATE TABLE copies (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        book_id BIGINT NOT NULL,
                        library_id BIGINT NOT NULL,
                        status VARCHAR(50) NOT NULL,
                        CONSTRAINT fk_copy_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
                        CONSTRAINT fk_copy_library FOREIGN KEY (library_id) REFERENCES libraries(id) ON DELETE CASCADE
);
