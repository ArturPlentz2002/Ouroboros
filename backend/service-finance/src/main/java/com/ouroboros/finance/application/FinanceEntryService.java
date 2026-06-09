package com.ouroboros.finance.application;

import com.ouroboros.finance.adapter.out.messaging.OutboxWriter;
import com.ouroboros.finance.adapter.out.persistence.CategoryRepository;
import com.ouroboros.finance.adapter.out.persistence.FinanceEntryRepository;
import com.ouroboros.finance.domain.EntryType;
import com.ouroboros.finance.domain.FinanceEntry;
import com.ouroboros.shared.events.FinanceChangeType;
import com.ouroboros.shared.events.FinanceEntryEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Casos de uso de lancamentos. Todas as operacoes sao escopadas pelo usuario dono. */
@Service
public class FinanceEntryService {

  private static final String AGGREGATE = "FinanceEntry";

  private final FinanceEntryRepository entries;
  private final CategoryRepository categories;
  private final OutboxWriter outbox;

  public FinanceEntryService(
      FinanceEntryRepository entries, CategoryRepository categories, OutboxWriter outbox) {
    this.entries = entries;
    this.categories = categories;
    this.outbox = outbox;
  }

  @Transactional
  public FinanceEntry create(
      UUID userId,
      UUID categoryId,
      EntryType type,
      BigDecimal amount,
      String description,
      LocalDate occurredOn,
      UUID eventId) {
    requireOwnedCategory(userId, categoryId);
    FinanceEntry saved =
        entries.save(
            FinanceEntry.create(
                userId, categoryId, type, amount, description, occurredOn, eventId));
    publish(saved, FinanceChangeType.CREATED);
    return saved;
  }

  @Transactional(readOnly = true)
  public List<FinanceEntry> list(UUID userId) {
    return entries.findByUserIdOrderByOccurredOnDescCreatedAtDesc(userId);
  }

  @Transactional(readOnly = true)
  public FinanceEntry get(UUID userId, UUID id) {
    return entries.findByIdAndUserId(id, userId).orElseThrow(() -> new EntryNotFoundException(id));
  }

  @Transactional
  public FinanceEntry update(
      UUID userId,
      UUID id,
      UUID categoryId,
      EntryType type,
      BigDecimal amount,
      String description,
      LocalDate occurredOn,
      UUID eventId) {
    FinanceEntry entry =
        entries.findByIdAndUserId(id, userId).orElseThrow(() -> new EntryNotFoundException(id));
    requireOwnedCategory(userId, categoryId);
    entry.update(categoryId, type, amount, description, occurredOn, eventId);
    FinanceEntry saved = entries.save(entry);
    publish(saved, FinanceChangeType.UPDATED);
    return saved;
  }

  @Transactional
  public void delete(UUID userId, UUID id) {
    FinanceEntry entry =
        entries.findByIdAndUserId(id, userId).orElseThrow(() -> new EntryNotFoundException(id));
    entries.delete(entry);
    publish(entry, FinanceChangeType.DELETED);
  }

  /** Enfileira o evento de mudanca no outbox (mesma transacao da escrita). */
  private void publish(FinanceEntry entry, FinanceChangeType change) {
    UUID eventId = UUID.randomUUID();
    FinanceEntryEvent event =
        new FinanceEntryEvent(
            eventId.toString(),
            entry.getUserId(),
            entry.getId(),
            change,
            entry.getType().name(),
            entry.getAmount(),
            Instant.now());
    outbox.write(eventId, FinanceEntryEvent.topicFor(change), AGGREGATE, entry.getId(), event);
  }

  /** Quando informada, a categoria precisa existir e pertencer ao usuario. */
  private void requireOwnedCategory(UUID userId, UUID categoryId) {
    if (categoryId != null && categories.findByIdAndUserId(categoryId, userId).isEmpty()) {
      throw new CategoryNotFoundException(categoryId);
    }
  }
}
