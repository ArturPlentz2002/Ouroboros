CREATE TABLE refresh_tokens (
    id         UUID PRIMARY KEY,
    token_hash VARCHAR(64)  NOT NULL UNIQUE,
    user_id    UUID         NOT NULL REFERENCES users (id),
    expires_at TIMESTAMPTZ  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
