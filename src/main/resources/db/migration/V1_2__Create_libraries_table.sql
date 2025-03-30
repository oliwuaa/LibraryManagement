CREATE TABLE libraries (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           name VARCHAR(255) NOT NULL UNIQUE,
                           address VARCHAR(255) NOT NULL,
                           status VARCHAR(50) NOT NULL
);
