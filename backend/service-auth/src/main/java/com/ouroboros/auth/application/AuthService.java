package com.ouroboros.auth.application;

import com.ouroboros.auth.adapter.out.persistence.UserRepository;
import com.ouroboros.auth.domain.PasswordPolicy;
import com.ouroboros.auth.domain.User;
import java.time.Instant;
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

  public AuthService(UserRepository users, PasswordEncoder passwordEncoder) {
    this.users = users;
    this.passwordEncoder = passwordEncoder;
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
        new User(
            UUID.randomUUID(), normalizedEmail, passwordEncoder.encode(rawPassword), Instant.now());
    users.save(user);
    return new RegisteredUser(user.getId(), user.getEmail());
  }

  private static String normalizeEmail(String email) {
    return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
  }
}
