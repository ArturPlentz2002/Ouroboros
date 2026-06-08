package com.ouroboros.auth.application.social;

/** Lancada quando um ID token social e invalido. */
public class InvalidSocialTokenException extends RuntimeException {

  public InvalidSocialTokenException(String message) {
    super(message);
  }

  public InvalidSocialTokenException(String message, Throwable cause) {
    super(message, cause);
  }
}
