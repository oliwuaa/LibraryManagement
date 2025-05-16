ALTER TABLE refresh_tokens
ALTER COLUMN token TYPE VARCHAR(255) USING token::text;

ALTER TABLE refresh_tokens
    ALTER COLUMN token SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_refresh_token_token ON refresh_tokens(token);
