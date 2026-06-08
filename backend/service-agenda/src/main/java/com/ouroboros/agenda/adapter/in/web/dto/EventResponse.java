package com.ouroboros.agenda.adapter.in.web.dto;

import com.ouroboros.agenda.domain.AgendaEvent;
import java.time.Instant;
import java.util.UUID;

/** Representacao de um evento na API. */
public record EventResponse(
    UUID id,
    String title,
    String description,
    Instant startsAt,
    Instant endsAt,
    Instant createdAt) {

  public static EventResponse from(AgendaEvent event) {
    return new EventResponse(
        event.getId(),
        event.getTitle(),
        event.getDescription(),
        event.getStartsAt(),
        event.getEndsAt(),
        event.getCreatedAt());
  }
}
