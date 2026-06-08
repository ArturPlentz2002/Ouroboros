package com.ouroboros.auth.application;

/** Lancada quando o e-mail ja esta cadastrado. */
public class EmailAlreadyUsedException extends RuntimeException {

  public EmailAlreadyUsedException(String email) {
    super("E-mail ja cadastrado: " + email);
  }
}
