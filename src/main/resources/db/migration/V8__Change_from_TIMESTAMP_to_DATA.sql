ALTER TABLE reservations
ALTER COLUMN created_at TYPE DATE
USING created_at::DATE;

ALTER TABLE reservations
ALTER COLUMN expiration_date TYPE DATE
USING expiration_date::DATE;