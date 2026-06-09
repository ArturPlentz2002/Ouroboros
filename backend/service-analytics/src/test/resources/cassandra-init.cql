CREATE KEYSPACE IF NOT EXISTS ouroboros WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

CREATE TABLE IF NOT EXISTS ouroboros.user_activity (
  user_id     uuid,
  occurred_at timestamp,
  event_id    text,
  event_type  text,
  source      text,
  PRIMARY KEY ((user_id), occurred_at, event_id)
) WITH CLUSTERING ORDER BY (occurred_at DESC, event_id ASC);
