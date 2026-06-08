package com.ouroboros.auth.application;

/** Lancada quando o refresh token e invalido, expirado ou ja utilizado. */
public class InvalidRefreshTokenException extends RuntimeException {

  public InvalidRefreshTokenException() {
    super("Refresh token invalido ou expirado");
  }
}
