package com.ouroboros.finance.application;

import java.util.UUID;

/** Lancada quando o lancamento nao existe para o usuario informado. */
public class EntryNotFoundException extends RuntimeException {

  public EntryNotFoundException(UUID id) {
    super("Lancamento nao encontrado: " + id);
  }
}
