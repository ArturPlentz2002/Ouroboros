package com.ouroboros.user.adapter.in.web.dto;

import com.ouroboros.user.domain.UserProfile;
import java.time.Instant;
import java.util.UUID;

/** Representacao do perfil na API. */
public record ProfileResponse(
    UUID userId,
    String email,
    String displayName,
    String avatarUrl,
    Instant createdAt,
    Instant updatedAt) {

  public static ProfileResponse from(UserProfile profile) {
    return new ProfileResponse(
        profile.getUserId(),
        profile.getEmail(),
        profile.getDisplayName(),
        profile.getAvatarUrl(),
        profile.getCreatedAt(),
        profile.getUpdatedAt());
  }
}
