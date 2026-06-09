package com.ouroboros.analytics.adapter.in.web;

import com.ouroboros.analytics.adapter.in.web.dto.ActivityResponse;
import com.ouroboros.analytics.application.AnalyticsService;
import com.ouroboros.shared.ApiPaths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/analytics")
public class AnalyticsController {

  private final AnalyticsService analyticsService;

  public AnalyticsController(AnalyticsService analyticsService) {
    this.analyticsService = analyticsService;
  }

  @GetMapping("/activity")
  public List<ActivityResponse> activity(@AuthenticationPrincipal Jwt jwt) {
    return analyticsService.listActivity(userId(jwt)).stream().map(ActivityResponse::from).toList();
  }

  @GetMapping("/summary")
  public Map<String, Long> summary(@AuthenticationPrincipal Jwt jwt) {
    return analyticsService.summary(userId(jwt));
  }

  private static UUID userId(Jwt jwt) {
    return UUID.fromString(jwt.getSubject());
  }
}
