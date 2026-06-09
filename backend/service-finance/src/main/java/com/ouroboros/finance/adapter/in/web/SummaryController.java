package com.ouroboros.finance.adapter.in.web;

import com.ouroboros.finance.adapter.in.web.dto.MonthlySummaryResponse;
import com.ouroboros.finance.application.MonthlySummaryService;
import com.ouroboros.shared.ApiPaths;
import java.time.YearMonth;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/finance")
public class SummaryController {

  private final MonthlySummaryService summaryService;

  public SummaryController(MonthlySummaryService summaryService) {
    this.summaryService = summaryService;
  }

  /** Resumo do mes informado (formato {@code YYYY-MM}); ausente, usa o mes atual. */
  @GetMapping("/summary")
  public MonthlySummaryResponse summary(
      @AuthenticationPrincipal Jwt jwt, @RequestParam(required = false) String month) {
    YearMonth target =
        (month == null || month.isBlank()) ? YearMonth.now() : YearMonth.parse(month);
    return MonthlySummaryResponse.from(summaryService.summarize(userId(jwt), target));
  }

  private static UUID userId(Jwt jwt) {
    return UUID.fromString(jwt.getSubject());
  }
}
