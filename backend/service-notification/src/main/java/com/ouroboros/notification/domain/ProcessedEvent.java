package com.ouroboros.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Registro de evento ja processado (idempotencia do consumidor). A entrega Kafka e at-least-once;
 * guardar o id do evento garante efeito exactly-once no dominio.
 */
@Entity
@Table(name = "processed_events")
public class ProcessedEvent {

  @Id
  @Column(name = "event_id")
  private UUID eventId;

  @Column(name = "processed_at", nullable = false)
  private Instant processedAt;

  protected ProcessedEvent() {
    // exigido pelo JPA
  }

  public ProcessedEvent(UUID eventId) {
    this.eventId = eventId;
    this.processedAt = Instant.now();
  }

  public UUID getEventId() {
    return eventId;
  }

  public Instant getProcessedAt() {
    return processedAt;
  }
}
