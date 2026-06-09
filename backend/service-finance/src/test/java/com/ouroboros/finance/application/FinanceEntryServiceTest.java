package com.ouroboros.finance.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ouroboros.finance.adapter.out.messaging.OutboxWriter;
import com.ouroboros.finance.adapter.out.persistence.CategoryRepository;
import com.ouroboros.finance.adapter.out.persistence.FinanceEntryRepository;
import com.ouroboros.finance.domain.Category;
import com.ouroboros.finance.domain.EntryType;
import com.ouroboros.finance.domain.FinanceEntry;
import com.ouroboros.shared.events.FinanceEntryEvent;
import com.ouroboros.shared.events.Topics;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FinanceEntryServiceTest {

  @Mock private FinanceEntryRepository entries;
  @Mock private CategoryRepository categories;
  @Mock private OutboxWriter outbox;
  @InjectMocks private FinanceEntryService service;

  private final UUID userId = UUID.randomUUID();
  private final LocalDate today = LocalDate.of(2026, 6, 1);

  @Test
  void criaSemCategoriaNaoConsultaCategorias() {
    when(entries.save(any(FinanceEntry.class))).thenAnswer(inv -> inv.getArgument(0));

    FinanceEntry created =
        service.create(
            userId, null, EntryType.EXPENSE, new BigDecimal("12.50"), "Cafe", today, null);

    assertThat(created.getCategoryId()).isNull();
    assertThat(created.getType()).isEqualTo(EntryType.EXPENSE);
    assertThat(created.getAmount()).isEqualByComparingTo("12.50");
    verifyNoInteractions(categories);
    verify(entries).save(any(FinanceEntry.class));
    verify(outbox)
        .write(
            any(),
            eq(Topics.FINANCE_ENTRY_CREATED),
            eq("FinanceEntry"),
            eq(created.getId()),
            any(FinanceEntryEvent.class));
  }

  @Test
  void criaComCategoriaPropriaSalva() {
    Category category = Category.create(userId, "Mercado", null);
    UUID categoryId = category.getId();
    when(categories.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(category));
    when(entries.save(any(FinanceEntry.class))).thenAnswer(inv -> inv.getArgument(0));

    FinanceEntry created =
        service.create(
            userId, categoryId, EntryType.EXPENSE, new BigDecimal("99.90"), null, today, null);

    assertThat(created.getCategoryId()).isEqualTo(categoryId);
    verify(entries).save(any(FinanceEntry.class));
  }

  @Test
  void rejeitaCategoriaInexistenteOuDeOutroUsuario() {
    UUID categoryId = UUID.randomUUID();
    when(categories.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                service.create(
                    userId, categoryId, EntryType.INCOME, new BigDecimal("10"), null, today, null))
        .isInstanceOf(CategoryNotFoundException.class);
    verify(entries, never()).save(any(FinanceEntry.class));
  }

  @Test
  void getInexistenteLancaNotFound() {
    UUID id = UUID.randomUUID();
    when(entries.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.get(userId, id)).isInstanceOf(EntryNotFoundException.class);
  }

  @Test
  void atualizaInexistenteLancaNotFound() {
    UUID id = UUID.randomUUID();
    when(entries.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                service.update(
                    userId, id, null, EntryType.EXPENSE, new BigDecimal("5"), null, today, null))
        .isInstanceOf(EntryNotFoundException.class);
    verify(entries, never()).save(any(FinanceEntry.class));
  }

  @Test
  void deletaInexistenteLancaNotFound() {
    UUID id = UUID.randomUUID();
    when(entries.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.delete(userId, id)).isInstanceOf(EntryNotFoundException.class);
    verify(entries, never()).delete(any(FinanceEntry.class));
  }

  @Test
  void deletaPublicaEventoDeRemocao() {
    FinanceEntry entry =
        FinanceEntry.create(
            userId, null, EntryType.EXPENSE, new BigDecimal("5"), null, today, null);
    UUID id = entry.getId();
    when(entries.findByIdAndUserId(id, userId)).thenReturn(Optional.of(entry));

    service.delete(userId, id);

    verify(entries).delete(entry);
    verify(outbox)
        .write(
            any(),
            eq(Topics.FINANCE_ENTRY_DELETED),
            eq("FinanceEntry"),
            eq(id),
            any(FinanceEntryEvent.class));
  }
}
