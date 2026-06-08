CREATE TABLE agenda_events (
    id          UUID PRIMARY KEY,
    user_id     UUID          NOT NULL,
    title       VARCHAR(255)  NOT NULL,
    description VARCHAR(2000),
    starts_at   TIMESTAMPTZ   NOT NULL,
    ends_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_agenda_events_user_id ON agenda_events (user_id);
