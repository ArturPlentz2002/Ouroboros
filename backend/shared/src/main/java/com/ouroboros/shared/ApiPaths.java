package com.ouroboros.shared;

/** Constantes compartilhadas de versionamento/prefixos de API entre os servicos. */
public final class ApiPaths {

  /** Prefixo de versao da API REST. */
  public static final String API_V1 = "/api/v1";

  private ApiPaths() {
    throw new AssertionError("Classe utilitaria, nao instanciar");
  }
}
