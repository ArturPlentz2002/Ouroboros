package com.ouroboros.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ouroboros.auth.adapter.out.messaging.OutboxWriter;
import com.ouroboros.auth.adapter.out.persistence.UserRepository;
import com.ouroboros.auth.application.social.SocialIdTokenVerifier;
import com.ouroboros.auth.application.social.SocialIdentity;
import com.ouroboros.auth.application.social.UnsupportedSocialProviderException;
import com.ouroboros.auth.domain.User;
import com.ouroboros.auth.domain.WeakPasswordException;
import com.ouroboros.shared.events.Topics;
import com.ouroboros.shared.events.UserRegisteredEvent;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository users;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private TokenService tokenService;
  @Mock private RefreshTokenService refreshTokenService;
  @Mock private SocialIdTokenVerifier googleVerifier;
  @Mock private OutboxWriter outbox;
  private AuthService authService;

  @BeforeEach
  void setUp() {
    authService =
        new AuthService(
            users,
            passwordEncoder,
            tokenService,
            refreshTokenService,
            List.of(googleVerifier),
            outbox);
  }

  @Test
  void registraNovoUsuarioNormalizandoEmailEHasheandoSenha() {
    when(users.existsByEmail("ana@ouroboros.dev")).thenReturn(false);
    when(passwordEncoder.encode("password1")).thenReturn("HASH");

    RegisteredUser result = authService.register("  Ana@Ouroboros.DEV ", "password1");

    assertThat(result.email()).isEqualTo("ana@ouroboros.dev");
    ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
    verify(users).save(saved.capture());
    assertThat(saved.getValue().getEmail()).isEqualTo("ana@ouroboros.dev");
    assertThat(saved.getValue().getPasswordHash()).isEqualTo("HASH");
    assertThat(saved.getValue().getProvider()).isEqualTo(User.PROVIDER_LOCAL);
    verify(outbox)
        .write(
            any(),
            eq(Topics.USER_REGISTERED),
            eq("User"),
            eq(saved.getValue().getId()),
            any(UserRegisteredEvent.class));
  }

  @Test
  void rejeitaEmailDuplicado() {
    when(users.existsByEmail("dup@ouroboros.dev")).thenReturn(true);

    assertThatThrownBy(() -> authService.register("dup@ouroboros.dev", "password1"))
        .isInstanceOf(EmailAlreadyUsedException.class);
    verify(users, never()).save(any());
  }

  @Test
  void rejeitaSenhaFraca() {
    assertThatThrownBy(() -> authService.register("ana@ouroboros.dev", "1234567"))
        .isInstanceOf(WeakPasswordException.class);
    verify(users, never()).save(any());
  }

  @Test
  void loginComCredenciaisValidasEmiteTokens() {
    UUID userId = UUID.randomUUID();
    User user = User.local(userId, "ana@ouroboros.dev", "HASH", Instant.now());
    when(users.findByEmail("ana@ouroboros.dev")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password1", "HASH")).thenReturn(true);
    when(tokenService.issueAccessToken(userId, "ana@ouroboros.dev")).thenReturn("ACCESS");
    when(refreshTokenService.issue(userId)).thenReturn("REFRESH");
    when(tokenService.accessTokenTtlSeconds()).thenReturn(900L);

    TokenPair pair = authService.login("Ana@Ouroboros.DEV", "password1");

    assertThat(pair.accessToken()).isEqualTo("ACCESS");
    assertThat(pair.refreshToken()).isEqualTo("REFRESH");
  }

  @Test
  void loginComSenhaErradaLanca() {
    User user = User.local(UUID.randomUUID(), "ana@ouroboros.dev", "HASH", Instant.now());
    when(users.findByEmail("ana@ouroboros.dev")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("errada", "HASH")).thenReturn(false);

    assertThatThrownBy(() -> authService.login("ana@ouroboros.dev", "errada"))
        .isInstanceOf(InvalidCredentialsException.class);
  }

  @Test
  void loginDeContaSocialSemSenhaLanca() {
    User social =
        User.social(UUID.randomUUID(), "ana@ouroboros.dev", "GOOGLE", "sub", Instant.now());
    when(users.findByEmail("ana@ouroboros.dev")).thenReturn(Optional.of(social));

    assertThatThrownBy(() -> authService.login("ana@ouroboros.dev", "qualquer1"))
        .isInstanceOf(InvalidCredentialsException.class);
  }

  @Test
  void refreshRotacionaEEmiteNovoAccessToken() {
    UUID userId = UUID.randomUUID();
    User user = User.local(userId, "ana@ouroboros.dev", "HASH", Instant.now());
    when(refreshTokenService.rotate("OLD"))
        .thenReturn(new RefreshTokenService.Rotation(userId, "NEW_REFRESH"));
    when(users.findById(userId)).thenReturn(Optional.of(user));
    when(tokenService.issueAccessToken(userId, "ana@ouroboros.dev")).thenReturn("NEW_ACCESS");
    when(tokenService.accessTokenTtlSeconds()).thenReturn(900L);

    TokenPair pair = authService.refresh("OLD");

    assertThat(pair.accessToken()).isEqualTo("NEW_ACCESS");
    assertThat(pair.refreshToken()).isEqualTo("NEW_REFRESH");
  }

  @Test
  void loginSocialCriaUsuarioQuandoNaoExiste() {
    when(googleVerifier.provider()).thenReturn("GOOGLE");
    when(googleVerifier.verify("tok"))
        .thenReturn(new SocialIdentity("GOOGLE", "g-sub", "Social@Google.com"));
    when(users.findByEmail("social@google.com")).thenReturn(Optional.empty());
    when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    when(tokenService.issueAccessToken(any(UUID.class), eq("social@google.com")))
        .thenReturn("ACCESS");
    when(refreshTokenService.issue(any(UUID.class))).thenReturn("REFRESH");
    when(tokenService.accessTokenTtlSeconds()).thenReturn(900L);

    TokenPair pair = authService.loginWithSocial("google", "tok");

    assertThat(pair.accessToken()).isEqualTo("ACCESS");
    ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
    verify(users).save(saved.capture());
    assertThat(saved.getValue().getProvider()).isEqualTo("GOOGLE");
    assertThat(saved.getValue().getExternalId()).isEqualTo("g-sub");
    assertThat(saved.getValue().getPasswordHash()).isNull();
  }

  @Test
  void loginSocialAssociaUsuarioExistentePorEmail() {
    UUID existingId = UUID.randomUUID();
    User existing = User.local(existingId, "social@google.com", "HASH", Instant.now());
    when(googleVerifier.provider()).thenReturn("GOOGLE");
    when(googleVerifier.verify("tok"))
        .thenReturn(new SocialIdentity("GOOGLE", "g-sub", "social@google.com"));
    when(users.findByEmail("social@google.com")).thenReturn(Optional.of(existing));
    when(tokenService.issueAccessToken(existingId, "social@google.com")).thenReturn("ACCESS");
    when(refreshTokenService.issue(existingId)).thenReturn("REFRESH");
    when(tokenService.accessTokenTtlSeconds()).thenReturn(900L);

    TokenPair pair = authService.loginWithSocial("google", "tok");

    assertThat(pair.accessToken()).isEqualTo("ACCESS");
    verify(users, never()).save(any());
  }

  @Test
  void loginSocialComProvedorNaoSuportadoLanca() {
    when(googleVerifier.provider()).thenReturn("GOOGLE");

    assertThatThrownBy(() -> authService.loginWithSocial("facebook", "tok"))
        .isInstanceOf(UnsupportedSocialProviderException.class);
  }
}
