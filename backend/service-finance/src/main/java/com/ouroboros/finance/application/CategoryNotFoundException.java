package com.ouroboros.finance.application;

import java.util.UUID;

/** Lancada quando a categoria nao existe para o usuario informado. */
public class CategoryNotFoundException extends RuntimeException {

  public CategoryNotFoundException(UUID id) {
    super("Categoria nao encontrada: " + id);
  }
}
