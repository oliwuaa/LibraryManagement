CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       name VARCHAR(100),
                       surname VARCHAR(100),
                       role VARCHAR(50) NOT NULL,
                       library_id BIGINT,
                       CONSTRAINT fk_user_library FOREIGN KEY (library_id) REFERENCES libraries(id) ON DELETE SET NULL
);
