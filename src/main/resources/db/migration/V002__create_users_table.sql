CREATE TABLE IF NOT EXISTS users
(
    user_id                     uuid               PRIMARY KEY,
    client_id                   uuid               NOT NULL,
    client_user_id              uuid               NOT NULL,
    blocked                     timestamptz        NULL,
    blocked_reason              text               NULL,
    created                     timestamptz        NOT NULL,
    one_off_ais                 boolean            NOT NULL default false
);

CREATE INDEX IF NOT EXISTS idx_client_id ON users (client_id);
CREATE INDEX IF NOT EXISTS idx_client_user_id ON users (client_user_id);
CREATE INDEX IF NOT EXISTS idx_created ON users USING brin(created);
