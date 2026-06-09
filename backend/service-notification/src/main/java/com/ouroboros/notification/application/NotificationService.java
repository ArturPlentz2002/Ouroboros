package com.ouroboros.notification.application;

import com.ouroboros.notification.adapter.out.messaging.EmailTaskPublisher;
import com.ouroboros.notification.adapter.out.persistence.NotificationRepository;
import com.ouroboros.notification.adapter.out.persistence.ProcessedEventRepository;
import com.ouroboros.notification.domain.Notification;
import com.ouroboros.notification.domain.ProcessedEvent;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
   * retorna vazio (consumo at-least-once + dedup = efeito exactly-once). Retorna a notificacao
   * criada para que o chamador enfileire o e-mail APOS o commit (ver {@link
   * #enqueueEmail(Notification)}).
   */
  @Transactional
  public Optional<Notification> record(
      UUID eventId, UUID userId, String type, String title, String message) {
    if (processed.existsById(eventId)) {
      return Optional.empty();
    }
    Notification notification =
        notifications.save(Notification.create(userId, type, title, message));
    processed.save(new ProcessedEvent(eventId));
    return Optional.of(notification);
  }

  /**
   * Enfileira a tarefa de e-mail (RabbitMQ). Deve ser chamado APOS o commit de {@link #record},
   * para que o worker enxergue a notificacao ja persistida ao processar a tarefa.
   */
  public void enqueueEmail(Notification notification) {
    emailPublisher.enqueue(
        new EmailTask(notification.getId(), notification.getUserId(), notification.getTitle()));
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
