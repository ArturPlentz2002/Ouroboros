package com.ouroboros.auth.domain;

/** Politica minima de senha. Regra de dominio pura (testavel sem Spring). */
public final class PasswordPolicy {

  public static final int MIN_LENGTH = 8;

  private PasswordPolicy() {
    throw new AssertionError("Classe utilitaria, nao instanciar");
  }

  /**
   * Valida a senha em texto puro.
   *
   * @throws WeakPasswordException se a senha for nula ou curta demais
   */
  public static void validate(String rawPassword) {
    if (rawPassword == null || rawPassword.length() < MIN_LENGTH) {
      throw new WeakPasswordException("A senha deve ter ao menos " + MIN_LENGTH + " caracteres");
    }
  }
}
