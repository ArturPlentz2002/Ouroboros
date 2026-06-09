package com.ouroboros.finance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Lancamento financeiro (receita ou despesa), sempre pertencente a um usuario. */
@Entity
@Table(name = "finance_entries")
public class FinanceEntry {

  @Id private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  /** Categoria opcional; pode ficar nula (lancamento sem categoria). */
  @Column(name = "category_id")
  private UUID categoryId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EntryType type;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  @Column(length = 255)
  private String description;

  @Column(name = "occurred_on", nullable = false)
  private LocalDate occurredOn;

  /** Vinculo opcional com um evento da agenda (por id, sem join cross-servico). */
  @Column(name = "event_id")
  private UUID eventId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected FinanceEntry() {
    // exigido pelo JPA
  }

  private FinanceEntry(
      UUID id,
      UUID userId,
      UUID categoryId,
      EntryType type,
      BigDecimal amount,
      String description,
      LocalDate occurredOn,
      UUID eventId,
      Instant createdAt) {
    this.id = id;
    this.userId = userId;
    this.categoryId = categoryId;
    this.type = type;
    this.amount = amount;
    this.description = description;
    this.occurredOn = occurredOn;
    this.eventId = eventId;
    this.createdAt = createdAt;
  }

  /** Cria um novo lancamento para o usuario. */
  public static FinanceEntry create(
      UUID userId,
      UUID categoryId,
      EntryType type,
      BigDecimal amount,
      String description,
      LocalDate occurredOn,
      UUID eventId) {
    return new FinanceEntry(
        UUID.randomUUID(),
        userId,
        categoryId,
        type,
        amount,
        description,
        occurredOn,
        eventId,
        Instant.now());
  }

  /** Atualiza os campos editaveis do lancamento. */
  public void update(
      UUID categoryId,
      EntryType type,
      BigDecimal amount,
      String description,
      LocalDate occurredOn,
      UUID eventId) {
    this.categoryId = categoryId;
    this.type = type;
    this.amount = amount;
    this.description = description;
    this.occurredOn = occurredOn;
    this.eventId = eventId;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public UUID getCategoryId() {
    return categoryId;
  }

  public EntryType getType() {
    return type;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String getDescription() {
    return description;
  }

  public LocalDate getOccurredOn() {
    return occurredOn;
  }

  public UUID getEventId() {
    return eventId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
