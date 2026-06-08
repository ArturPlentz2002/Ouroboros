package com.ouroboros.auth.application;

/** Par de tokens emitido no login/refresh. */
public record TokenPair(String accessToken, String refreshToken, long expiresInSeconds) {}
