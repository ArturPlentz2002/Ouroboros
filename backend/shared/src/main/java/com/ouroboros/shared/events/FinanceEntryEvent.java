package com.ouroboros.shared.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento publicado nas mudancas de um lancamento financeiro. O {@link #changeType} define o topico
 * de destino (ver {@link #topicFor(FinanceChangeType)}).
 *
 * @param eventId id unico do evento (idempotencia)
 * @param userId dono do lancamento
 * @param entryId id do lancamento
 * @param changeType tipo de mudanca (CREATED/UPDATED/DELETED)
 * @param type natureza do lancamento (EXPENSE/INCOME) como texto
 * @param amount valor do lancamento
 * @param occurredAt momento da mudanca
 */
public record FinanceEntryEvent(
    String eventId,
    UUID userId,
    UUID entryId,
    FinanceChangeType changeType,
    String type,
    BigDecimal amount,
    Instant occurredAt) {

  /** Topico Kafka correspondente a um tipo de mudanca. */
  public static String topicFor(FinanceChangeType changeType) {
    return switch (changeType) {
      case CREATED -> Topics.FINANCE_ENTRY_CREATED;
      case UPDATED -> Topics.FINANCE_ENTRY_UPDATED;
      case DELETED -> Topics.FINANCE_ENTRY_DELETED;
    };
  }
}
