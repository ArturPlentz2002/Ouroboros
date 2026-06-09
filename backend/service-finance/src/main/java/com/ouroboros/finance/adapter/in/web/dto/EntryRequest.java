package com.ouroboros.finance.adapter.in.web.dto;

import com.ouroboros.finance.domain.EntryType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Payload de criacao/atualizacao de lancamento. */
public record EntryRequest(
    UUID categoryId,
    @NotNull EntryType type,
    @NotNull @Positive BigDecimal amount,
    @Size(max = 255) String description,
    @NotNull LocalDate occurredOn,
    UUID eventId) {}
