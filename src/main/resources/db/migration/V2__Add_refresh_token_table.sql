CREATE TABLE refresh_tokens (
                                id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                user_id    BIGINT NOT NULL,
                                created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                                expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token_user_id ON refresh_tokens(user_id);