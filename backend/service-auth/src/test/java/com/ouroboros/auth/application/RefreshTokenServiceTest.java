package com.ouroboros.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ouroboros.auth.adapter.out.persistence.RefreshTokenRepository;
import com.ouroboros.auth.domain.RefreshToken;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

  @Mock private RefreshTokenRepository repository;
  private RefreshTokenService service;

  @BeforeEach
  void setUp() {
    service = new RefreshTokenService(repository, Duration.ofDays(30));
  }

  private static RefreshToken token(UUID userId, Instant expiresAt) {
    return new RefreshToken(UUID.randomUUID(), "hash", userId, expiresAt, Instant.now());
  }

  @Test
  void issueSalvaHashERetornaValorCru() {
    UUID userId = UUID.randomUUID();

    String raw = service.issue(userId);

    ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
    verify(repository).save(captor.capture());
    RefreshToken stored = captor.getValue();
    assertThat(raw).isNotBlank();
    assertThat(stored.getTokenHash()).isNotBlank().isNotEqualTo(raw);
    assertThat(stored.getUserId()).isEqualTo(userId);
    assertThat(stored.getExpiresAt()).isAfter(Instant.now());
  }

  @Test
  void rotateConsomeAntigoEEmiteNovo() {
    UUID userId = UUID.randomUUID();
    when(repository.findByTokenHash(anyString()))
        .thenReturn(Optional.of(token(userId, Instant.now().plus(Duration.ofDays(1)))));
    when(repository.consumeByTokenHash(anyString())).thenReturn(1);

    RefreshTokenService.Rotation rotation = service.rotate("raw");

    assertThat(rotation.userId()).isEqualTo(userId);
    assertThat(rotation.newRawToken()).isNotBlank();
    verify(repository).consumeByTokenHash(anyString());
    verify(repository).save(any()); // novo token emitido
  }

  @Test
  void rotateComTokenInexistenteLanca() {
    when(repository.findByTokenHash(anyString())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.rotate("inexistente"))
        .isInstanceOf(InvalidRefreshTokenException.class);
    verify(repository, never()).save(any());
  }

  @Test
  void rotateComTokenExpiradoConsomeELanca() {
    UUID userId = UUID.randomUUID();
    when(repository.findByTokenHash(anyString()))
        .thenReturn(Optional.of(token(userId, Instant.now().minusSeconds(10))));

    assertThatThrownBy(() -> service.rotate("expirado"))
        .isInstanceOf(InvalidRefreshTokenException.class);
    verify(repository).consumeByTokenHash(anyString());
    verify(repository, never()).save(any());
  }

  @Test
  void rotatePerdeCorridaQuandoJaConsumido() {
    UUID userId = UUID.randomUUID();
    when(repository.findByTokenHash(anyString()))
        .thenReturn(Optional.of(token(userId, Instant.now().plus(Duration.ofDays(1)))));
    when(repository.consumeByTokenHash(anyString())).thenReturn(0);

    assertThatThrownBy(() -> service.rotate("perdida"))
        .isInstanceOf(InvalidRefreshTokenException.class);
    verify(repository, never()).save(any()); // nao emitiu novo token
  }
}
