package com.ouroboros.analytics.domain;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

/** Chave composta de {@link UserActivity}: particao por usuario, clusterizada por instante. */
@PrimaryKeyClass
public class UserActivityKey implements Serializable {

  @PrimaryKeyColumn(name = "user_id", type = PrimaryKeyType.PARTITIONED)
  private UUID userId;

  @PrimaryKeyColumn(name = "occurred_at", ordinal = 0, ordering = Ordering.DESCENDING)
  private Instant occurredAt;

  @PrimaryKeyColumn(name = "event_id", ordinal = 1)
  private String eventId;

  protected UserActivityKey() {
    // exigido pelo mapeador
  }

  public UserActivityKey(UUID userId, Instant occurredAt, String eventId) {
    this.userId = userId;
    this.occurredAt = occurredAt;
    this.eventId = eventId;
  }

  public UUID getUserId() {
    return userId;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }

  public String getEventId() {
    return eventId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UserActivityKey that)) {
      return false;
    }
    return Objects.equals(userId, that.userId)
        && Objects.equals(occurredAt, that.occurredAt)
        && Objects.equals(eventId, that.eventId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, occurredAt, eventId);
  }
}
