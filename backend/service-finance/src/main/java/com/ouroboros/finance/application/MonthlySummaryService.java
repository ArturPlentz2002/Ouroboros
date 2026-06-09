package com.ouroboros.finance.application;

import com.ouroboros.finance.adapter.out.persistence.CategoryRepository;
import com.ouroboros.finance.adapter.out.persistence.FinanceEntryRepository;
import com.ouroboros.finance.application.MonthlySummary.CategoryTotal;
import com.ouroboros.finance.domain.Category;
import com.ouroboros.finance.domain.EntryType;
import com.ouroboros.finance.domain.FinanceEntry;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Calcula o resumo financeiro mensal de um usuario ("quanto gastei este mes"). */
@Service
public class MonthlySummaryService {

  private final FinanceEntryRepository entries;
  private final CategoryRepository categories;

  public MonthlySummaryService(FinanceEntryRepository entries, CategoryRepository categories) {
    this.entries = entries;
    this.categories = categories;
  }

  @Transactional(readOnly = true)
  public MonthlySummary summarize(UUID userId, YearMonth month) {
    List<FinanceEntry> monthEntries =
        entries.findByUserIdAndOccurredOnBetween(userId, month.atDay(1), month.atEndOfMonth());
    Map<UUID, String> names =
        categories.findByUserIdOrderByNameAsc(userId).stream()
            .collect(Collectors.toMap(Category::getId, Category::getName));

    BigDecimal totalIncome = sumOf(monthEntries, EntryType.INCOME);
    BigDecimal totalExpense = sumOf(monthEntries, EntryType.EXPENSE);

    List<CategoryTotal> byCategory =
        monthEntries.stream()
            .collect(
                Collectors.groupingBy(
                    GroupKey::of,
                    Collectors.reducing(BigDecimal.ZERO, FinanceEntry::getAmount, BigDecimal::add)))
            .entrySet()
            .stream()
            .map(
                e ->
                    new CategoryTotal(
                        e.getKey().categoryId(),
                        names.get(e.getKey().categoryId()),
                        e.getKey().type(),
                        scaled(e.getValue())))
            .sorted(
                Comparator.comparing(CategoryTotal::type)
                    .thenComparing(
                        CategoryTotal::categoryName,
                        Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();

    return new MonthlySummary(
        month, totalIncome, totalExpense, scaled(totalIncome.subtract(totalExpense)), byCategory);
  }

  private static BigDecimal sumOf(List<FinanceEntry> entries, EntryType type) {
    return scaled(
        entries.stream()
            .filter(e -> e.getType() == type)
            .map(FinanceEntry::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
  }

  private static BigDecimal scaled(BigDecimal value) {
    return value.setScale(2, RoundingMode.HALF_UP);
  }

  /** Chave de agrupamento por categoria (pode ser nula) e tipo. */
  private record GroupKey(UUID categoryId, EntryType type) {
    static GroupKey of(FinanceEntry entry) {
      return new GroupKey(entry.getCategoryId(), entry.getType());
    }
  }
}
