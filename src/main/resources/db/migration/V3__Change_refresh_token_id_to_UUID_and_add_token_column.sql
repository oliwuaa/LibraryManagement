CREATE EXTENSION IF NOT EXISTS "pgcrypto";

ALTER TABLE refresh_tokens RENAME COLUMN id TO old_id;

ALTER TABLE refresh_tokens ADD COLUMN token UUID;

UPDATE refresh_tokens SET token = gen_random_uuid();

ALTER TABLE refresh_tokens DROP CONSTRAINT refresh_tokens_pkey;
ALTER TABLE refresh_tokens ADD PRIMARY KEY (token);

ALTER TABLE refresh_tokens DROP COLUMN old_id;