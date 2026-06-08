package com.ouroboros.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Perfil do usuario, identificado pelo mesmo id do JWT (subject). */
@Entity
@Table(name = "user_profiles")
public class UserProfile {

  @Id
  @Column(name = "user_id")
  private UUID userId;

  @Column(nullable = false)
  private String email;

  @Column(name = "display_name", nullable = false, length = 100)
  private String displayName;

  @Column(name = "avatar_url", length = 500)
  private String avatarUrl;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected UserProfile() {
    // exigido pelo JPA
  }

  public UserProfile(
      UUID userId,
      String email,
      String displayName,
      String avatarUrl,
      Instant createdAt,
      Instant updatedAt) {
    this.userId = userId;
    this.email = email;
    this.displayName = displayName;
    this.avatarUrl = avatarUrl;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  /** Atualiza os campos editaveis do perfil. */
  public void update(String displayName, String avatarUrl, Instant updatedAt) {
    this.displayName = displayName;
    this.avatarUrl = avatarUrl;
    this.updatedAt = updatedAt;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getEmail() {
    return email;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
