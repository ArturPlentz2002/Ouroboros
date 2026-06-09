package com.ouroboros.auth.application;

import com.ouroboros.auth.adapter.out.persistence.RefreshTokenRepository;
import com.ouroboros.auth.domain.RefreshToken;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Emissao e rotacao de refresh tokens opacos (persistidos como hash). */
@Service
public class RefreshTokenService {

  private final RefreshTokenRepository repository;
  private final Duration refreshTokenTtl;
  private final SecureRandom secureRandom = new SecureRandom();

  public RefreshTokenService(
      RefreshTokenRepository repository,
      @Value("${ouroboros.jwt.refresh-token-ttl:P30D}") Duration refreshTokenTtl) {
    this.repository = repository;
    this.refreshTokenTtl = refreshTokenTtl;
  }

  /** Cria e persiste um novo refresh token, retornando o valor cru (mostrado uma unica vez). */
  @Transactional
  public String issue(UUID userId) {
    String rawToken = generateRawToken();
    Instant now = Instant.now();
    RefreshToken token =
        new RefreshToken(UUID.randomUUID(), hash(rawToken), userId, now.plus(refreshTokenTtl), now);
    repository.save(token);
    return rawToken;
  }

  /**
   * Valida e rotaciona o refresh token: invalida o atual e emite um novo.
   *
   * @return id do usuario e o novo refresh token cru
   * @throws InvalidRefreshTokenException se o token nao existir ou estiver expirado
   */
  @Transactional
  public Rotation rotate(String rawToken) {
    String tokenHash = hash(rawToken);
    RefreshToken current =
        repository.findByTokenHash(tokenHash).orElseThrow(InvalidRefreshTokenException::new);
    if (current.getExpiresAt().isBefore(Instant.now())) {
      repository.consumeByTokenHash(tokenHash);
      throw new InvalidRefreshTokenException();
    }
    // Consumo atomico: em corrida concorrente, so uma transacao deleta a linha (retorna 1);
    // as demais recebem 0 e falham, garantindo rotacao de uso unico.
    if (repository.consumeByTokenHash(tokenHash) == 0) {
      throw new InvalidRefreshTokenException();
    }
    String newRawToken = issue(current.getUserId());
    return new Rotation(current.getUserId(), newRawToken);
  }

  /** Revoga (invalida) um refresh token, se existir. Idempotente. */
  @Transactional
  public void revoke(String rawToken) {
    repository.consumeByTokenHash(hash(rawToken));
  }

  private String generateRawToken() {
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private static String hash(String rawToken) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException("SHA-256 indisponivel", e);
    }
  }

  /** Resultado da rotacao de um refresh token. */
  public record Rotation(UUID userId, String newRawToken) {}
}
