package com.ouroboros.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ouroboros.auth.adapter.out.persistence.UserRepository;
import com.ouroboros.auth.domain.User;
import com.ouroboros.auth.domain.WeakPasswordException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository users;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private TokenService tokenService;
  @Mock private RefreshTokenService refreshTokenService;
  @InjectMocks private AuthService authService;

  @Test
  void registraNovoUsuarioNormalizandoEmailEHasheandoSenha() {
    when(users.existsByEmail("ana@ouroboros.dev")).thenReturn(false);
    when(passwordEncoder.encode("password1")).thenReturn("HASH");

    RegisteredUser result = authService.register("  Ana@Ouroboros.DEV ", "password1");

    assertThat(result.email()).isEqualTo("ana@ouroboros.dev");
    assertThat(result.id()).isNotNull();

    ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
    verify(users).save(saved.capture());
    assertThat(saved.getValue().getEmail()).isEqualTo("ana@ouroboros.dev");
    assertThat(saved.getValue().getPasswordHash()).isEqualTo("HASH");
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
    User user = new User(userId, "ana@ouroboros.dev", "HASH", Instant.now());
    when(users.findByEmail("ana@ouroboros.dev")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password1", "HASH")).thenReturn(true);
    when(tokenService.issueAccessToken(userId, "ana@ouroboros.dev")).thenReturn("ACCESS");
    when(refreshTokenService.issue(userId)).thenReturn("REFRESH");
    when(tokenService.accessTokenTtlSeconds()).thenReturn(900L);

    TokenPair pair = authService.login("Ana@Ouroboros.DEV", "password1");

    assertThat(pair.accessToken()).isEqualTo("ACCESS");
    assertThat(pair.refreshToken()).isEqualTo("REFRESH");
    assertThat(pair.expiresInSeconds()).isEqualTo(900L);
  }

  @Test
  void loginComSenhaErradaLanca() {
    UUID userId = UUID.randomUUID();
    User user = new User(userId, "ana@ouroboros.dev", "HASH", Instant.now());
    when(users.findByEmail("ana@ouroboros.dev")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("errada", "HASH")).thenReturn(false);

    assertThatThrownBy(() -> authService.login("ana@ouroboros.dev", "errada"))
        .isInstanceOf(InvalidCredentialsException.class);
  }

  @Test
  void loginComEmailInexistenteLanca() {
    when(users.findByEmail("x@ouroboros.dev")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login("x@ouroboros.dev", "password1"))
        .isInstanceOf(InvalidCredentialsException.class);
  }

  @Test
  void refreshRotacionaEEmiteNovoAccessToken() {
    UUID userId = UUID.randomUUID();
    User user = new User(userId, "ana@ouroboros.dev", "HASH", Instant.now());
    when(refreshTokenService.rotate("OLD"))
        .thenReturn(new RefreshTokenService.Rotation(userId, "NEW_REFRESH"));
    when(users.findById(userId)).thenReturn(Optional.of(user));
    when(tokenService.issueAccessToken(userId, "ana@ouroboros.dev")).thenReturn("NEW_ACCESS");
    when(tokenService.accessTokenTtlSeconds()).thenReturn(900L);

    TokenPair pair = authService.refresh("OLD");

    assertThat(pair.accessToken()).isEqualTo("NEW_ACCESS");
    assertThat(pair.refreshToken()).isEqualTo("NEW_REFRESH");
  }
}
