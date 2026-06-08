package com.ouroboros.auth.application.social;

/** Porta para validar ID tokens de provedores sociais. */
public interface SocialIdTokenVerifier {

  /** Nome do provedor suportado (ex.: GOOGLE). */
  String provider();

  /**
   * Valida o ID token e retorna a identidade.
   *
   * @throws InvalidSocialTokenException se o token for invalido (assinatura, iss, aud, exp, etc.)
   */
  SocialIdentity verify(String idToken);
}
