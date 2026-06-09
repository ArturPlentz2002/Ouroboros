package com.ouroboros.finance.adapter.in.web.dto;

import com.ouroboros.finance.application.MonthlySummary;
import com.ouroboros.finance.domain.EntryType;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Resumo financeiro mensal exposto pela API. */
public record MonthlySummaryResponse(
    String month,
    BigDecimal totalIncome,
    BigDecimal totalExpense,
    BigDecimal balance,
    List<CategoryTotalResponse> byCategory) {

  /** Total por categoria (ou sem categoria) e tipo. */
  public record CategoryTotalResponse(
      UUID categoryId, String categoryName, EntryType type, BigDecimal total) {}

  public static MonthlySummaryResponse from(MonthlySummary summary) {
    List<CategoryTotalResponse> byCategory =
        summary.byCategory().stream()
            .map(
                c ->
                    new CategoryTotalResponse(
                        c.categoryId(), c.categoryName(), c.type(), c.total()))
            .toList();
    return new MonthlySummaryResponse(
        summary.month().toString(),
        summary.totalIncome(),
        summary.totalExpense(),
        summary.balance(),
        byCategory);
  }
}
