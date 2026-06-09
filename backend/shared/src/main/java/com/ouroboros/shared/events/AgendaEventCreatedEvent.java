package com.ouroboros.shared.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento publicado quando um evento da agenda e criado.
 *
 * @param eventId id unico do evento de mensageria (idempotencia)
 * @param userId dono do evento da agenda
 * @param agendaEventId id do evento da agenda
 * @param title titulo do evento
 * @param startsAt inicio do evento
 * @param occurredAt momento da criacao
 */
public record AgendaEventCreatedEvent(
    String eventId,
    UUID userId,
    UUID agendaEventId,
    String title,
    Instant startsAt,
    Instant occurredAt) {}
