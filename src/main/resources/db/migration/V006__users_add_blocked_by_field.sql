ALTER TABLE users
    ADD COLUMN IF NOT EXISTS blocked_by TEXT;
