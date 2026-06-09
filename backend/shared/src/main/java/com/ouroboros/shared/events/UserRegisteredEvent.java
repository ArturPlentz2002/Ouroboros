package com.ouroboros.shared.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento publicado quando um usuario se registra.
 *
 * @param eventId id unico do evento (usado para idempotencia no consumidor)
 * @param userId id do usuario criado
 * @param email e-mail do usuario
 * @param occurredAt momento do registro
 */
public record UserRegisteredEvent(String eventId, UUID userId, String email, Instant occurredAt) {}
