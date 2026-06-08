package com.ouroboros.agenda.application;

import java.util.UUID;

/** Lancada quando o evento nao existe para o usuario informado. */
public class EventNotFoundException extends RuntimeException {

  public EventNotFoundException(UUID id) {
    super("Evento nao encontrado: " + id);
  }
}
