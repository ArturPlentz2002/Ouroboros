package com.ouroboros.auth.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PasswordPolicyTest {

  @Test
  void aceitaSenhaComOitoOuMaisCaracteres() {
    assertThatCode(() -> PasswordPolicy.validate("12345678")).doesNotThrowAnyException();
  }

  @Test
  void rejeitaSenhaCurta() {
    assertThatThrownBy(() -> PasswordPolicy.validate("1234567"))
        .isInstanceOf(WeakPasswordException.class);
  }

  @Test
  void rejeitaSenhaNula() {
    assertThatThrownBy(() -> PasswordPolicy.validate(null))
        .isInstanceOf(WeakPasswordException.class);
  }
}
