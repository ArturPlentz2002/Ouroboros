package com.ouroboros.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Notificacao gerada a partir de um evento de dominio, pertencente a um usuario. */
@Entity
@Table(name = "notifications")
public class Notification {

  @Id private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(nullable = false, length = 50)
  private String type;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(length = 1000)
  private String message;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  /** Momento em que a tarefa de e-mail foi processada (RabbitMQ); nulo = ainda nao enviada. */
  @Column(name = "emailed_at")
  private Instant emailedAt;

  protected Notification() {
    // exigido pelo JPA
  }

  private Notification(
      UUID id, UUID userId, String type, String title, String message, Instant createdAt) {
    this.id = id;
    this.userId = userId;
    this.type = type;
    this.title = title;
    this.message = message;
    this.createdAt = createdAt;
  }

  public static Notification create(UUID userId, String type, String title, String message) {
    return new Notification(UUID.randomUUID(), userId, type, title, message, Instant.now());
  }

  public void markEmailed(Instant when) {
    this.emailedAt = when;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getType() {
    return type;
  }

  public String getTitle() {
    return title;
  }

  public String getMessage() {
    return message;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getEmailedAt() {
    return emailedAt;
  }
}
