package com.ouroboros.notes.application;

/** Lancada quando a nota nao existe para o usuario informado. */
public class NoteNotFoundException extends RuntimeException {

  public NoteNotFoundException(String id) {
    super("Nota nao encontrada: " + id);
  }
}
