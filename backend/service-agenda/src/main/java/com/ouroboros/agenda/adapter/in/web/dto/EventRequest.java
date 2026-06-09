package com.ouroboros.agenda.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/** Payload de criacao/atualizacao de evento. */
public record EventRequest(
    @NotBlank String title, String description, @NotNull Instant startsAt, Instant endsAt) {}
