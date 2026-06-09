CREATE TABLE categories (
    id         UUID PRIMARY KEY,
    user_id    UUID         NOT NULL,
    name       VARCHAR(100) NOT NULL,
    color      VARCHAR(7),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_categories_user_id ON categories (user_id);

-- Nome unico por usuario (case-insensitive), espelhando a regra do dominio.
CREATE UNIQUE INDEX uq_categories_user_name ON categories (user_id, lower(name));
