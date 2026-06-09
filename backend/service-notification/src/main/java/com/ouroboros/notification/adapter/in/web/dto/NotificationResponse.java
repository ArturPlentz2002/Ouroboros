package com.ouroboros.notification.adapter.in.web.dto;

import com.ouroboros.notification.domain.Notification;
import java.time.Instant;
import java.util.UUID;

/** Representacao de uma notificacao na API. */
public record NotificationResponse(
    UUID id, String type, String title, String message, Instant createdAt, Instant emailedAt) {

  public static NotificationResponse from(Notification n) {
    return new NotificationResponse(
        n.getId(), n.getType(), n.getTitle(), n.getMessage(), n.getCreatedAt(), n.getEmailedAt());
  }
}
