CREATE TABLE reservations (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              user_id BIGINT NOT NULL,
                              copy_id BIGINT NOT NULL,
                              created_at TIMESTAMP NOT NULL,
                              expiration_date TIMESTAMP NOT NULL,
                              status VARCHAR(50),
                              CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                              CONSTRAINT fk_reservation_copy FOREIGN KEY (copy_id) REFERENCES copies(id) ON DELETE CASCADE
);
