package com.ouroboros.notification.adapter.in.messaging;

import com.ouroboros.notification.application.EmailTask;
import com.ouroboros.notification.application.NotificationService;
import com.ouroboros.shared.events.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Worker da fila {@code notifications.email}: "envia" o e-mail e marca a notificacao como enviada.
 */
@Component
public class EmailTaskListener {

  private static final Logger log = LoggerFactory.getLogger(EmailTaskListener.class);

  private final NotificationService service;

  public EmailTaskListener(NotificationService service) {
    this.service = service;
  }

  @RabbitListener(queues = Queues.NOTIFICATIONS_EMAIL)
  public void onEmailTask(EmailTask task) {
    // MVP: apenas registra o "envio". Aqui entraria a integracao real de e-mail.
    log.info("Enviando e-mail (simulado) ao usuario {}: {}", task.userId(), task.subject());
    service.markEmailed(task.notificationId());
  }
}
