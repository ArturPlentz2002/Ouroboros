package com.ouroboros.auth.domain;

/** Lancada quando a senha nao atende a politica minima. */
public class WeakPasswordException extends RuntimeException {

  public WeakPasswordException(String message) {
    super(message);
  }
}
