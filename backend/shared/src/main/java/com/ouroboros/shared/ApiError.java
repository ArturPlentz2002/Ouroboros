package com.ouroboros.shared;

import java.time.Instant;
import java.util.List;

/**
 * Corpo de erro padronizado das APIs REST.
 *
 * @param timestamp momento do erro
 * @param status codigo HTTP
 * @param error nome curto do erro
 * @param message mensagem legivel
 * @param path caminho da requisicao
 * @param details detalhes opcionais (ex.: erros de validacao por campo)
 */
public record ApiError(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path,
    List<String> details) {

  public static ApiError of(int status, String error, String message, String path) {
    return new ApiError(Instant.now(), status, error, message, path, List.of());
  }

  public static ApiError of(
      int status, String error, String message, String path, List<String> details) {
    return new ApiError(Instant.now(), status, error, message, path, details);
  }
}
