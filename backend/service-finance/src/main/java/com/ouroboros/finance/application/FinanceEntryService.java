package com.ouroboros.finance.application;

import com.ouroboros.finance.adapter.out.persistence.CategoryRepository;
import com.ouroboros.finance.adapter.out.persistence.FinanceEntryRepository;
import com.ouroboros.finance.domain.EntryType;
import com.ouroboros.finance.domain.FinanceEntry;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Casos de uso de lancamentos. Todas as operacoes sao escopadas pelo usuario dono. */
@Service
public class FinanceEntryService {

  private final FinanceEntryRepository entries;
  private final CategoryRepository categories;

  public FinanceEntryService(FinanceEntryRepository entries, CategoryRepository categories) {
    this.entries = entries;
    this.categories = categories;
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
    return entries.save(
        FinanceEntry.create(userId, categoryId, type, amount, description, occurredOn, eventId));
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
    return entries.save(entry);
  }

  @Transactional
  public void delete(UUID userId, UUID id) {
    FinanceEntry entry =
        entries.findByIdAndUserId(id, userId).orElseThrow(() -> new EntryNotFoundException(id));
    entries.delete(entry);
  }

  /** Quando informada, a categoria precisa existir e pertencer ao usuario. */
  private void requireOwnedCategory(UUID userId, UUID categoryId) {
    if (categoryId != null && categories.findByIdAndUserId(categoryId, userId).isEmpty()) {
      throw new CategoryNotFoundException(categoryId);
    }
  }
}
