ALTER TABLE users
    ADD COLUMN IF NOT EXISTS deleted timestamptz;
