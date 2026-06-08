package com.ouroboros.auth.application.social;

/** Lancada quando o provedor social informado nao e suportado. */
public class UnsupportedSocialProviderException extends RuntimeException {

  public UnsupportedSocialProviderException(String provider) {
    super("Provedor social nao suportado: " + provider);
  }
}
