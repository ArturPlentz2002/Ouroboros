package com.ouroboros.finance.adapter.out.messaging;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Linha da tabela de outbox transacional. Gravada na MESMA transacao da mudanca de dominio, garante
 * que o evento so existe se a transacao commitou (sem dual-write). O {@link OutboxRelay} publica os
 * pendentes no Kafka e marca {@link #sentAt}.
 */
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

  /** Id do evento — tambem usado como chave de idempotencia pelo consumidor. */
  @Id private UUID id;

  @Column(name = "aggregate_type", nullable = false, length = 100)
  private String aggregateType;

  @Column(name = "aggregate_id", nullable = false)
  private UUID aggregateId;

  @Column(nullable = false)
  private String topic;

  @Column(nullable = false, columnDefinition = "text")
  private String payload;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "sent_at")
  private Instant sentAt;

  protected OutboxEvent() {
    // exigido pelo JPA
  }

  private OutboxEvent(
      UUID id,
      String aggregateType,
      UUID aggregateId,
      String topic,
      String payload,
      Instant createdAt) {
    this.id = id;
    this.aggregateType = aggregateType;
    this.aggregateId = aggregateId;
    this.topic = topic;
    this.payload = payload;
    this.createdAt = createdAt;
  }

  public static OutboxEvent create(
      UUID id, String aggregateType, UUID aggregateId, String topic, String payload) {
    return new OutboxEvent(id, aggregateType, aggregateId, topic, payload, Instant.now());
  }

  /** Marca o evento como publicado. */
  public void markSent(Instant when) {
    this.sentAt = when;
  }

  public UUID getId() {
    return id;
  }

  public String getAggregateType() {
    return aggregateType;
  }

  public UUID getAggregateId() {
    return aggregateId;
  }

  public String getTopic() {
    return topic;
  }

  public String getPayload() {
    return payload;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getSentAt() {
    return sentAt;
  }
}
