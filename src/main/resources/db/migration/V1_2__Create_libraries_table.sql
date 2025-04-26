CREATE TABLE libraries (
                           id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           name VARCHAR(255) NOT NULL UNIQUE,
                           address VARCHAR(255) NOT NULL,
                           status VARCHAR(50) NOT NULL
);
