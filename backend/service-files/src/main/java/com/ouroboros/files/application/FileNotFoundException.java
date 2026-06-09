package com.ouroboros.files.application;

import java.util.UUID;

/** Lancada quando o arquivo nao existe para o usuario informado. */
public class FileNotFoundException extends RuntimeException {

  public FileNotFoundException(UUID id) {
    super("Arquivo nao encontrado: " + id);
  }
}
