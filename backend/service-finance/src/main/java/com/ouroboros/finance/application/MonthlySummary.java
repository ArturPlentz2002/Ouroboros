package com.ouroboros.finance.application;

import com.ouroboros.finance.domain.EntryType;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

/** Resumo financeiro de um mes: totais e quebra por categoria. */
public record MonthlySummary(
    YearMonth month,
    BigDecimal totalIncome,
    BigDecimal totalExpense,
    BigDecimal balance,
    List<CategoryTotal> byCategory) {

  /** Total de uma categoria (ou sem categoria, quando {@code categoryId} e nulo) por tipo. */
  public record CategoryTotal(
      UUID categoryId, String categoryName, EntryType type, BigDecimal total) {}
}
