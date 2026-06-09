package com.ouroboros.auth.application.social;

/**
 * Identidade extraida de um ID token social validado.
 *
 * @param provider provedor (ex.: GOOGLE)
 * @param externalId identificador do usuario no provedor (ex.: 'sub')
 * @param email e-mail verificado
 */
public record SocialIdentity(String provider, String externalId, String email) {}
