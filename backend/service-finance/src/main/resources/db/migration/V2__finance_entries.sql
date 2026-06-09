CREATE TABLE finance_entries (
    id          UUID PRIMARY KEY,
    user_id     UUID           NOT NULL,
    category_id UUID           REFERENCES categories (id) ON DELETE SET NULL,
    type        VARCHAR(20)    NOT NULL,
    amount      NUMERIC(15, 2) NOT NULL,
    description VARCHAR(255),
    occurred_on DATE           NOT NULL,
    event_id    UUID,
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_finance_entries_user_id ON finance_entries (user_id);
CREATE INDEX idx_finance_entries_user_occurred ON finance_entries (user_id, occurred_on);
CREATE INDEX idx_finance_entries_category_id ON finance_entries (category_id);
