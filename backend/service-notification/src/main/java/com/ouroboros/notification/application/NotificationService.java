package com.ouroboros.notification.application;

import com.ouroboros.notification.adapter.out.messaging.EmailTaskPublisher;
import com.ouroboros.notification.adapter.out.persistence.NotificationRepository;
import com.ouroboros.notification.adapter.out.persistence.ProcessedEventRepository;
import com.ouroboros.notification.domain.Notification;
import com.ouroboros.notification.domain.ProcessedEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cria notificacoes a partir de eventos (idempotente) e dispara a tarefa de e-mail. */
@Service
public class NotificationService {

  private final NotificationRepository notifications;
  private final ProcessedEventRepository processed;
  private final EmailTaskPublisher emailPublisher;

  public NotificationService(
      NotificationRepository notifications,
      ProcessedEventRepository processed,
      EmailTaskPublisher emailPublisher) {
    this.notifications = notifications;
    this.processed = processed;
    this.emailPublisher = emailPublisher;
  }

  /**
   * Registra uma notificacao para um evento. Idempotente: se o {@code eventId} ja foi processado,
   * nao faz nada (consumo at-least-once + dedup = efeito exactly-once).
   */
  @Transactional
  public void record(UUID eventId, UUID userId, String type, String title, String message) {
    if (processed.existsById(eventId)) {
      return;
    }
    Notification notification =
        notifications.save(Notification.create(userId, type, title, message));
    processed.save(new ProcessedEvent(eventId));
    emailPublisher.enqueue(new EmailTask(notification.getId(), userId, title));
  }

  /** Marca a notificacao como "enviada por e-mail" (consumo da fila RabbitMQ). */
  @Transactional
  public void markEmailed(UUID notificationId) {
    notifications.findById(notificationId).ifPresent(n -> n.markEmailed(Instant.now()));
  }

  @Transactional(readOnly = true)
  public List<Notification> list(UUID userId) {
    return notifications.findByUserIdOrderByCreatedAtDesc(userId);
  }
}
