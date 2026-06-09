package com.ouroboros.finance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Categoria de lancamentos financeiros, sempre pertencente a um usuario. */
@Entity
@Table(name = "categories")
public class Category {

  @Id private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(nullable = false, length = 100)
  private String name;

  /** Cor em hexadecimal (ex.: {@code #4caf50}); opcional, usada pelo app. */
  @Column(length = 7)
  private String color;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected Category() {
    // exigido pelo JPA
  }

  private Category(UUID id, UUID userId, String name, String color, Instant createdAt) {
    this.id = id;
    this.userId = userId;
    this.name = name;
    this.color = color;
    this.createdAt = createdAt;
  }

  /** Cria uma nova categoria para o usuario. */
  public static Category create(UUID userId, String name, String color) {
    return new Category(UUID.randomUUID(), userId, name, color, Instant.now());
  }

  /** Atualiza os campos editaveis da categoria. */
  public void update(String name, String color) {
    this.name = name;
    this.color = color;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getName() {
    return name;
  }

  public String getColor() {
    return color;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
