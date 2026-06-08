package com.ouroboros.auth.application;

/** Lancada quando e-mail/senha nao conferem. */
public class InvalidCredentialsException extends RuntimeException {

  public InvalidCredentialsException() {
    super("Credenciais invalidas");
  }
}
