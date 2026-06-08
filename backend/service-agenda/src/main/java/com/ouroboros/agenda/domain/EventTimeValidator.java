package com.ouroboros.agenda.domain;

import java.time.Instant;

/** Regra de dominio: o fim do evento (quando informado) deve ser apos o inicio. */
public final class EventTimeValidator {

  private EventTimeValidator() {
    throw new AssertionError("Classe utilitaria, nao instanciar");
  }

  public static void validate(Instant startsAt, Instant endsAt) {
    if (startsAt == null) {
      throw new InvalidEventTimeException("O inicio do evento e obrigatorio");
    }
    if (endsAt != null && !endsAt.isAfter(startsAt)) {
      throw new InvalidEventTimeException("O fim do evento deve ser apos o inicio");
    }
  }
}
