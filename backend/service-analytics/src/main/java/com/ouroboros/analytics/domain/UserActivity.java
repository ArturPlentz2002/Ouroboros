package com.ouroboros.analytics.domain;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/** Registro de atividade de um usuario, derivado de um evento da plataforma. */
@Table("user_activity")
public class UserActivity {

  @PrimaryKey private UserActivityKey key;

  @Column("event_type")
  private String eventType;

  @Column("source")
  private String source;

  protected UserActivity() {
    // exigido pelo mapeador
  }

  public UserActivity(UserActivityKey key, String eventType, String source) {
    this.key = key;
    this.eventType = eventType;
    this.source = source;
  }

  /** Fabrica a partir dos campos do evento. */
  public static UserActivity of(
      UUID userId, String eventId, String eventType, String source, Instant occurredAt) {
    return new UserActivity(new UserActivityKey(userId, occurredAt, eventId), eventType, source);
  }

  public UserActivityKey getKey() {
    return key;
  }

  public String getEventType() {
    return eventType;
  }

  public String getSource() {
    return source;
  }
}
