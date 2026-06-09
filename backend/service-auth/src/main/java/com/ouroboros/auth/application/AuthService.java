package com.ouroboros.auth.application;

import com.ouroboros.auth.adapter.out.messaging.OutboxWriter;
import com.ouroboros.auth.adapter.out.persistence.UserRepository;
import com.ouroboros.auth.application.RefreshTokenService.Rotation;
import com.ouroboros.auth.application.social.SocialIdTokenVerifier;
import com.ouroboros.auth.application.social.SocialIdentity;
import com.ouroboros.auth.application.social.UnsupportedSocialProviderException;
import com.ouroboros.auth.domain.PasswordPolicy;
import com.ouroboros.auth.domain.User;
import com.ouroboros.shared.events.Topics;
import com.ouroboros.shared.events.UserRegisteredEvent;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Casos de uso de autenticacao. */
@Service
public class AuthService {

  private final UserRepository users;
  private final PasswordEncoder passwordEncoder;
  private final TokenService tokenService;
  private final RefreshTokenService refreshTokenService;
  private final List<SocialIdTokenVerifier> socialVerifiers;
  private final OutboxWriter outbox;

  public AuthService(
      UserRepository users,
      PasswordEncoder passwordEncoder,
      TokenService tokenService,
      RefreshTokenService refreshTokenService,
      List<SocialIdTokenVerifier> socialVerifiers,
      OutboxWriter outbox) {
    this.users = users;
    this.passwordEncoder = passwordEncoder;
    this.tokenService = tokenService;
    this.refreshTokenService = refreshTokenService;
    this.socialVerifiers = socialVerifiers;
    this.outbox = outbox;
  }

  /**
   * Registra um novo usuario com e-mail e senha.
   *
   * @throws com.ouroboros.auth.domain.WeakPasswordException se a senha for fraca
   * @throws EmailAlreadyUsedException se o e-mail ja existir
   */
  @Transactional
  public RegisteredUser register(String email, String rawPassword) {
    PasswordPolicy.validate(rawPassword);
    String normalizedEmail = normalizeEmail(email);
    if (users.existsByEmail(normalizedEmail)) {
      throw new EmailAlreadyUsedException(normalizedEmail);
    }
    User user =
        User.local(
            UUID.randomUUID(), normalizedEmail, passwordEncoder.encode(rawPassword), Instant.now());
    users.save(user);
    publishRegistered(user);
    return new RegisteredUser(user.getId(), user.getEmail());
  }

  /**
   * Autentica por e-mail/senha e emite access + refresh tokens.
   *
   * @throws InvalidCredentialsException se o e-mail nao existir, for conta social, ou a senha nao
   *     conferir
   */
  @Transactional
  public TokenPair login(String email, String rawPassword) {
    User user =
        users.findByEmail(normalizeEmail(email)).orElseThrow(InvalidCredentialsException::new);
    if (user.getPasswordHash() == null
        || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
      throw new InvalidCredentialsException();
    }
    return issueTokens(user.getId(), user.getEmail());
  }

  /**
   * Autentica via login social: valida o ID token do provedor, cria/associa o usuario (por e-mail)
   * e emite o JWT do Ouroboros.
   *
   * @throws UnsupportedSocialProviderException se o provedor nao for suportado
   * @throws com.ouroboros.auth.application.social.InvalidSocialTokenException se o token for
   *     invalido
   */
  @Transactional
  public TokenPair loginWithSocial(String provider, String idToken) {
    SocialIdTokenVerifier verifier =
        socialVerifiers.stream()
            .filter(v -> v.provider().equalsIgnoreCase(provider))
            .findFirst()
            .orElseThrow(() -> new UnsupportedSocialProviderException(provider));
    SocialIdentity identity = verifier.verify(idToken);
    String email = normalizeEmail(identity.email());
    User user = users.findByEmail(email).orElse(null);
    if (user == null) {
      user =
          users.save(
              User.social(
                  UUID.randomUUID(),
                  email,
                  identity.provider(),
                  identity.externalId(),
                  Instant.now()));
      publishRegistered(user);
    }
    return issueTokens(user.getId(), user.getEmail());
  }

  /**
   * Rotaciona o refresh token e emite um novo par de tokens.
   *
   * @throws InvalidRefreshTokenException se o refresh token for invalido/expirado
   */
  @Transactional
  public TokenPair refresh(String rawRefreshToken) {
    Rotation rotation = refreshTokenService.rotate(rawRefreshToken);
    User user = users.findById(rotation.userId()).orElseThrow(InvalidRefreshTokenException::new);
    String accessToken = tokenService.issueAccessToken(user.getId(), user.getEmail());
    return new TokenPair(accessToken, rotation.newRawToken(), tokenService.accessTokenTtlSeconds());
  }

  /** Revoga o refresh token informado (logout). Idempotente. */
  @Transactional
  public void logout(String rawRefreshToken) {
    refreshTokenService.revoke(rawRefreshToken);
  }

  private TokenPair issueTokens(UUID userId, String email) {
    String accessToken = tokenService.issueAccessToken(userId, email);
    String refreshToken = refreshTokenService.issue(userId);
    return new TokenPair(accessToken, refreshToken, tokenService.accessTokenTtlSeconds());
  }

  /** Enfileira o evento user.registered no outbox (mesma transacao da criacao do usuario). */
  private void publishRegistered(User user) {
    UUID eventId = UUID.randomUUID();
    UserRegisteredEvent event =
        new UserRegisteredEvent(eventId.toString(), user.getId(), user.getEmail(), Instant.now());
    outbox.write(eventId, Topics.USER_REGISTERED, "User", user.getId(), event);
  }

  private static String normalizeEmail(String email) {
    return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
  }
}
