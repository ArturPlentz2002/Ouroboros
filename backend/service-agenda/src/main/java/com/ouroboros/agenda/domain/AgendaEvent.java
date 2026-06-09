package com.ouroboros.agenda.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Evento da agenda, sempre pertencente a um usuario. */
@Entity
@Table(name = "agenda_events")
public class AgendaEvent {

  @Id private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(nullable = false)
  private String title;

  @Column(length = 2000)
  private String description;

  @Column(name = "starts_at", nullable = false)
  private Instant startsAt;

  @Column(name = "ends_at")
  private Instant endsAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected AgendaEvent() {
    // exigido pelo JPA
  }

  private AgendaEvent(
      UUID id,
      UUID userId,
      String title,
      String description,
      Instant startsAt,
      Instant endsAt,
      Instant createdAt) {
    this.id = id;
    this.userId = userId;
    this.title = title;
    this.description = description;
    this.startsAt = startsAt;
    this.endsAt = endsAt;
    this.createdAt = createdAt;
  }

  /** Cria um novo evento, validando o intervalo de tempo. */
  public static AgendaEvent create(
      UUID userId, String title, String description, Instant startsAt, Instant endsAt) {
    EventTimeValidator.validate(startsAt, endsAt);
    return new AgendaEvent(
        UUID.randomUUID(), userId, title, description, startsAt, endsAt, Instant.now());
  }

  /** Atualiza os campos editaveis, validando o intervalo de tempo. */
  public void update(String title, String description, Instant startsAt, Instant endsAt) {
    EventTimeValidator.validate(startsAt, endsAt);
    this.title = title;
    this.description = description;
    this.startsAt = startsAt;
    this.endsAt = endsAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public Instant getStartsAt() {
    return startsAt;
  }

  public Instant getEndsAt() {
    return endsAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
