package com.ouroboros.auth.adapter.in.web.dto;

/** Resposta com os tokens emitidos. */
public record TokenResponse(
    String accessToken, String refreshToken, String tokenType, long expiresIn) {

  public static TokenResponse bearer(String accessToken, String refreshToken, long expiresIn) {
    return new TokenResponse(accessToken, refreshToken, "Bearer", expiresIn);
  }
}
