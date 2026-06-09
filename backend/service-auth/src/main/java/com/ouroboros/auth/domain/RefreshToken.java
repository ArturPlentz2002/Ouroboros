package com.ouroboros.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Refresh token persistido (armazena apenas o hash do token, nunca o valor cru). */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

  @Id private UUID id;

  @Column(name = "token_hash", nullable = false, unique = true)
  private String tokenHash;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected RefreshToken() {
    // exigido pelo JPA
  }

  public RefreshToken(
      UUID id, String tokenHash, UUID userId, Instant expiresAt, Instant createdAt) {
    this.id = id;
    this.tokenHash = tokenHash;
    this.userId = userId;
    this.expiresAt = expiresAt;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public UUID getUserId() {
    return userId;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
