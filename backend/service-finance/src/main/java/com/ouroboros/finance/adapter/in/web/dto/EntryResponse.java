package com.ouroboros.finance.adapter.in.web.dto;

import com.ouroboros.finance.domain.EntryType;
import com.ouroboros.finance.domain.FinanceEntry;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Representacao de um lancamento na API. */
public record EntryResponse(
    UUID id,
    UUID categoryId,
    EntryType type,
    BigDecimal amount,
    String description,
    LocalDate occurredOn,
    UUID eventId,
    Instant createdAt) {

  public static EntryResponse from(FinanceEntry entry) {
    return new EntryResponse(
        entry.getId(),
        entry.getCategoryId(),
        entry.getType(),
        entry.getAmount(),
        entry.getDescription(),
        entry.getOccurredOn(),
        entry.getEventId(),
        entry.getCreatedAt());
  }
}
