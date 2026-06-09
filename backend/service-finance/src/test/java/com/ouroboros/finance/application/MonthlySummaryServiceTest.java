package com.ouroboros.finance.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.ouroboros.finance.adapter.out.persistence.CategoryRepository;
import com.ouroboros.finance.adapter.out.persistence.FinanceEntryRepository;
import com.ouroboros.finance.domain.Category;
import com.ouroboros.finance.domain.EntryType;
import com.ouroboros.finance.domain.FinanceEntry;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MonthlySummaryServiceTest {

  @Mock private FinanceEntryRepository entries;
  @Mock private CategoryRepository categories;
  @InjectMocks private MonthlySummaryService service;

  private final UUID userId = UUID.randomUUID();
  private final YearMonth month = YearMonth.of(2026, 6);
  private final LocalDate day = LocalDate.of(2026, 6, 10);

  private FinanceEntry entry(UUID categoryId, EntryType type, String amount) {
    return FinanceEntry.create(userId, categoryId, type, new BigDecimal(amount), null, day, null);
  }

  @Test
  void calculaTotaisEQuebraPorCategoria() {
    Category mercado = Category.create(userId, "Mercado", null);
    Category salario = Category.create(userId, "Salario", null);
    UUID c1 = mercado.getId();
    UUID c2 = salario.getId();

    when(entries.findByUserIdAndOccurredOnBetween(userId, month.atDay(1), month.atEndOfMonth()))
        .thenReturn(
            List.of(
                entry(c1, EntryType.EXPENSE, "100.00"),
                entry(c1, EntryType.EXPENSE, "50.00"),
                entry(c2, EntryType.INCOME, "1000.00"),
                entry(null, EntryType.EXPENSE, "30.00")));
    when(categories.findByUserIdOrderByNameAsc(userId)).thenReturn(List.of(mercado, salario));

    MonthlySummary summary = service.summarize(userId, month);

    assertThat(summary.month()).isEqualTo(month);
    assertThat(summary.totalIncome()).isEqualByComparingTo("1000.00");
    assertThat(summary.totalExpense()).isEqualByComparingTo("180.00");
    assertThat(summary.balance()).isEqualByComparingTo("820.00");

    assertThat(summary.byCategory())
        .hasSize(3)
        .anySatisfy(
            ct -> {
              assertThat(ct.categoryId()).isEqualTo(c1);
              assertThat(ct.categoryName()).isEqualTo("Mercado");
              assertThat(ct.type()).isEqualTo(EntryType.EXPENSE);
              assertThat(ct.total()).isEqualByComparingTo("150.00");
            })
        .anySatisfy(
            ct -> {
              assertThat(ct.categoryId()).isNull();
              assertThat(ct.categoryName()).isNull();
              assertThat(ct.type()).isEqualTo(EntryType.EXPENSE);
              assertThat(ct.total()).isEqualByComparingTo("30.00");
            })
        .anySatisfy(
            ct -> {
              assertThat(ct.categoryId()).isEqualTo(c2);
              assertThat(ct.categoryName()).isEqualTo("Salario");
              assertThat(ct.type()).isEqualTo(EntryType.INCOME);
              assertThat(ct.total()).isEqualByComparingTo("1000.00");
            });
  }

  @Test
  void mesSemLancamentosRetornaZeros() {
    when(entries.findByUserIdAndOccurredOnBetween(userId, month.atDay(1), month.atEndOfMonth()))
        .thenReturn(List.of());
    when(categories.findByUserIdOrderByNameAsc(userId)).thenReturn(List.of());

    MonthlySummary summary = service.summarize(userId, month);

    assertThat(summary.totalIncome()).isEqualByComparingTo("0.00");
    assertThat(summary.totalExpense()).isEqualByComparingTo("0.00");
    assertThat(summary.balance()).isEqualByComparingTo("0.00");
    assertThat(summary.byCategory()).isEmpty();
  }
}
