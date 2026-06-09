package com.ouroboros.analytics.adapter.in.web.dto;

import com.ouroboros.analytics.domain.UserActivity;
import java.time.Instant;

/** Representacao de uma atividade na API. */
public record ActivityResponse(
    String eventId, String eventType, String source, Instant occurredAt) {

  public static ActivityResponse from(UserActivity activity) {
    return new ActivityResponse(
        activity.getKey().getEventId(),
        activity.getEventType(),
        activity.getSource(),
        activity.getKey().getOccurredAt());
  }
}
