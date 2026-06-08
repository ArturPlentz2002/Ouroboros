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
}
