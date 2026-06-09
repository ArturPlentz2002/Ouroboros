package com.ouroboros.analytics.application;

import com.ouroboros.analytics.adapter.out.persistence.UserActivityRepository;
import com.ouroboros.analytics.domain.UserActivity;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/** Casos de uso de analytics: registra e consulta a atividade do usuario. */
@Service
public class AnalyticsService {

  private final UserActivityRepository activities;

  public AnalyticsService(UserActivityRepository activities) {
    this.activities = activities;
  }

  /**
   * Registra uma atividade. Usa o {@code occurredAt} do evento, tornando o insert idempotente: o
   * mesmo evento reprocessado reescreve a mesma linha.
   */
  public UserActivity record(
      UUID userId, String eventId, String eventType, String source, Instant occurredAt) {
    return activities.save(UserActivity.of(userId, eventId, eventType, source, occurredAt));
  }

  public List<UserActivity> listActivity(UUID userId) {
    return activities.findByKeyUserId(userId);
  }

  /** Conta a atividade do usuario por tipo de evento. */
  public Map<String, Long> summary(UUID userId) {
    return listActivity(userId).stream()
        .collect(
            Collectors.groupingBy(
                UserActivity::getEventType, LinkedHashMap::new, Collectors.counting()));
  }
}
