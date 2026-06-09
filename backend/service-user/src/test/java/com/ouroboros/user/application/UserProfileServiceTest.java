package com.ouroboros.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ouroboros.user.adapter.out.persistence.UserProfileRepository;
import com.ouroboros.user.domain.UserProfile;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

  @Mock private UserProfileRepository profiles;
  @InjectMocks private UserProfileService service;

  @Test
  void getOrCreateCriaPerfilPadraoQuandoNaoExiste() {
    UUID userId = UUID.randomUUID();
    when(profiles.findById(userId)).thenReturn(Optional.empty());
    when(profiles.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

    UserProfile profile = service.getOrCreate(userId, "ana@ouroboros.dev");

    assertThat(profile.getUserId()).isEqualTo(userId);
    assertThat(profile.getEmail()).isEqualTo("ana@ouroboros.dev");
    assertThat(profile.getDisplayName()).isEqualTo("ana");
    verify(profiles).save(any(UserProfile.class));
  }

  @Test
  void getOrCreateRetornaExistenteSemSalvar() {
    UUID userId = UUID.randomUUID();
    UserProfile existing =
        new UserProfile(userId, "ana@ouroboros.dev", "Ana", null, Instant.now(), Instant.now());
    when(profiles.findById(userId)).thenReturn(Optional.of(existing));

    UserProfile profile = service.getOrCreate(userId, "ana@ouroboros.dev");

    assertThat(profile).isSameAs(existing);
    verify(profiles, never()).save(any());
  }

  @Test
  void updateAlteraCamposEditaveis() {
    UUID userId = UUID.randomUUID();
    UserProfile existing =
        new UserProfile(userId, "ana@ouroboros.dev", "Ana", null, Instant.now(), Instant.now());
    when(profiles.findById(userId)).thenReturn(Optional.of(existing));
    when(profiles.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

    UserProfile profile =
        service.update(userId, "ana@ouroboros.dev", "Ana Maria", "https://x/a.png");

    assertThat(profile.getDisplayName()).isEqualTo("Ana Maria");
    assertThat(profile.getAvatarUrl()).isEqualTo("https://x/a.png");
  }
}
