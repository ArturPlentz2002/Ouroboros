CREATE TABLE outbox_events (
    id             UUID PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id   UUID         NOT NULL,
    topic          VARCHAR(255) NOT NULL,
    payload        TEXT         NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    sent_at        TIMESTAMPTZ
);

-- O relay busca apenas os pendentes (sent_at IS NULL), mais antigos primeiro.
CREATE INDEX idx_outbox_pending ON outbox_events (created_at) WHERE sent_at IS NULL;
