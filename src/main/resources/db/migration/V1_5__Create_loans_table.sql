CREATE TABLE loans (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       user_id BIGINT NOT NULL,
                       copy_id BIGINT NOT NULL,
                       start_date DATE NOT NULL,
                       end_date DATE NOT NULL,
                       return_date DATE,
                       CONSTRAINT fk_loan_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                       CONSTRAINT fk_loan_copy FOREIGN KEY (copy_id) REFERENCES copies(id) ON DELETE CASCADE
);
