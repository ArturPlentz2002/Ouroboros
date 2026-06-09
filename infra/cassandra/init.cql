-- Keyspace e schema do service-analytics.
CREATE KEYSPACE IF NOT EXISTS ouroboros
  WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

-- Atividade do usuario derivada dos eventos da plataforma.
-- Particionada por usuario; clusterizada por instante (desc) para listar o mais recente.
-- Insert e idempotente: a mesma (user_id, occurred_at, event_id) reescreve a mesma linha.
CREATE TABLE IF NOT EXISTS ouroboros.user_activity (
  user_id     uuid,
  occurred_at timestamp,
  event_id    text,
  event_type  text,
  source      text,
  PRIMARY KEY ((user_id), occurred_at, event_id)
) WITH CLUSTERING ORDER BY (occurred_at DESC, event_id ASC);
