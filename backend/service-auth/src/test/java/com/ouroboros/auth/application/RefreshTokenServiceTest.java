package com.ouroboros.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
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
  void rotateInvalidaAntigoEEmiteNovo() {
    UUID userId = UUID.randomUUID();
    ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
    String raw = service.issue(userId);
    verify(repository).save(captor.capture());
    RefreshToken stored = captor.getValue();
    when(repository.findByTokenHash(stored.getTokenHash())).thenReturn(Optional.of(stored));

    RefreshTokenService.Rotation rotation = service.rotate(raw);

    assertThat(rotation.userId()).isEqualTo(userId);
    assertThat(rotation.newRawToken()).isNotBlank().isNotEqualTo(raw);
    verify(repository).delete(stored);
  }

  @Test
  void rotateComTokenInexistenteLanca() {
    when(repository.findByTokenHash(anyString())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.rotate("inexistente"))
        .isInstanceOf(InvalidRefreshTokenException.class);
  }

  @Test
  void rotateComTokenExpiradoLancaEDeleta() {
    UUID userId = UUID.randomUUID();
    ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
    String raw = service.issue(userId);
    verify(repository).save(captor.capture());
    String hash = captor.getValue().getTokenHash();
    RefreshToken expired =
        new RefreshToken(
            UUID.randomUUID(),
            hash,
            userId,
            Instant.now().minusSeconds(10),
            Instant.now().minusSeconds(100));
    when(repository.findByTokenHash(hash)).thenReturn(Optional.of(expired));

    assertThatThrownBy(() -> service.rotate(raw)).isInstanceOf(InvalidRefreshTokenException.class);
    verify(repository).delete(expired);
  }
}
