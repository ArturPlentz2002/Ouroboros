package com.ouroboros.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Usuario: credencial local (e-mail + hash) ou conta social (provider + external_id). */
@Entity
@Table(name = "users")
public class User {

  /** Provedor de identidade local (e-mail/senha). */
  public static final String PROVIDER_LOCAL = "LOCAL";

  @Id private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  /** Nulo para contas sociais. */
  @Column(name = "password_hash")
  private String passwordHash;

  @Column(nullable = false, length = 20)
  private String provider;

  /** Identificador do usuario no provedor social (ex.: 'sub' do Google). Nulo para LOCAL. */
  @Column(name = "external_id")
  private String externalId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected User() {
    // exigido pelo JPA
  }

  private User(
      UUID id,
      String email,
      String passwordHash,
      String provider,
      String externalId,
      Instant createdAt) {
    this.id = id;
    this.email = email;
    this.passwordHash = passwordHash;
    this.provider = provider;
    this.externalId = externalId;
    this.createdAt = createdAt;
  }

  /** Cria um usuario local (e-mail/senha). */
  public static User local(UUID id, String email, String passwordHash, Instant createdAt) {
    return new User(id, email, passwordHash, PROVIDER_LOCAL, null, createdAt);
  }

  /** Cria um usuario social (sem senha). */
  public static User social(
      UUID id, String email, String provider, String externalId, Instant createdAt) {
    return new User(id, email, null, provider, externalId, createdAt);
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

  public String getProvider() {
    return provider;
  }

  public String getExternalId() {
    return externalId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
