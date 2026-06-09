CREATE TABLE notifications (
    id         UUID PRIMARY KEY,
    user_id    UUID         NOT NULL,
    type       VARCHAR(50)  NOT NULL,
    title      VARCHAR(255) NOT NULL,
    message    VARCHAR(1000),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    emailed_at TIMESTAMPTZ
);

CREATE INDEX idx_notifications_user ON notifications (user_id, created_at DESC);

-- Idempotencia do consumidor: id de cada evento ja processado.
CREATE TABLE processed_events (
    event_id     UUID PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
