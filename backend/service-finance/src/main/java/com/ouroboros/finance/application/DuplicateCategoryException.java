package com.ouroboros.finance.application;

/** Lancada ao tentar criar/renomear uma categoria com nome ja usado pelo usuario. */
public class DuplicateCategoryException extends RuntimeException {

  public DuplicateCategoryException(String name) {
    super("Ja existe uma categoria com o nome: " + name);
  }
}
