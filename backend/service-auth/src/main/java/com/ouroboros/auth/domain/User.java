package com.ouroboros.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Credencial de usuario (e-mail + hash de senha). */
@Entity
@Table(name = "users")
public class User {

  @Id private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected User() {
    // exigido pelo JPA
  }

  public User(UUID id, String email, String passwordHash, Instant createdAt) {
    this.id = id;
    this.email = email;
    this.passwordHash = passwordHash;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
