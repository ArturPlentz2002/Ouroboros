-- Suporte a login social: usuarios sociais nao tem senha.
ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;
ALTER TABLE users ADD COLUMN provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL';
ALTER TABLE users ADD COLUMN external_id VARCHAR(255);

-- Um mesmo (provider, external_id) nunca se repete.
CREATE UNIQUE INDEX idx_users_provider_external_id
    ON users (provider, external_id)
    WHERE external_id IS NOT NULL;
