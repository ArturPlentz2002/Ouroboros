package com.ouroboros.agenda.domain;

/** Lancada quando o intervalo de tempo do evento e invalido. */
public class InvalidEventTimeException extends RuntimeException {

  public InvalidEventTimeException(String message) {
    super(message);
  }
}
