package com.ouroboros.user.application;

import com.ouroboros.user.adapter.out.persistence.UserProfileRepository;
import com.ouroboros.user.domain.UserProfile;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Casos de uso do perfil do usuario. O perfil e criado sob demanda a partir do JWT. */
@Service
public class UserProfileService {

  private final UserProfileRepository profiles;

  public UserProfileService(UserProfileRepository profiles) {
    this.profiles = profiles;
  }

  @Transactional
  public UserProfile getOrCreate(UUID userId, String email) {
    return profiles.findById(userId).orElseGet(() -> profiles.save(defaultProfile(userId, email)));
  }

  @Transactional
  public UserProfile update(UUID userId, String email, String displayName, String avatarUrl) {
    UserProfile profile = profiles.findById(userId).orElseGet(() -> defaultProfile(userId, email));
    profile.update(displayName, avatarUrl, Instant.now());
    return profiles.save(profile);
  }

  private static UserProfile defaultProfile(UUID userId, String email) {
    Instant now = Instant.now();
    return new UserProfile(userId, email, defaultDisplayName(email), null, now, now);
  }

  private static String defaultDisplayName(String email) {
    if (email == null || email.isBlank()) {
      return "usuario";
    }
    int at = email.indexOf('@');
    return at > 0 ? email.substring(0, at) : email;
  }
}
