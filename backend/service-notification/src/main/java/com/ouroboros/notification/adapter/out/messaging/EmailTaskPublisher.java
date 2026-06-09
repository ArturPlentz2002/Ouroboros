package com.ouroboros.notification.adapter.out.messaging;

import com.ouroboros.notification.application.EmailTask;
import com.ouroboros.shared.events.Queues;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/** Publica tarefas de e-mail na fila RabbitMQ {@code notifications.email}. */
@Component
public class EmailTaskPublisher {

  private final RabbitTemplate rabbit;

  public EmailTaskPublisher(RabbitTemplate rabbit) {
    this.rabbit = rabbit;
  }

  public void enqueue(EmailTask task) {
    // Exchange default ("") + routing key = nome da fila entrega direto na fila.
    rabbit.convertAndSend(Queues.NOTIFICATIONS_EMAIL, task);
  }
}
