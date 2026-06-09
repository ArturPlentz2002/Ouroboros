package com.ouroboros.notification.config;

import com.ouroboros.shared.events.Queues;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Filas RabbitMQ (com DLQ) e conversor JSON para as tarefas. */
@Configuration
public class RabbitConfig {

  /** Sufixo da dead-letter queue da fila de e-mails. */
  public static final String EMAIL_DLQ = Queues.NOTIFICATIONS_EMAIL + ".dlq";

  /** Fila principal: mensagens rejeitadas vao para a DLQ via exchange default. */
  @Bean
  public Queue emailQueue() {
    return QueueBuilder.durable(Queues.NOTIFICATIONS_EMAIL)
        .deadLetterExchange("")
        .deadLetterRoutingKey(EMAIL_DLQ)
        .build();
  }

  @Bean
  public Queue emailDeadLetterQueue() {
    return QueueBuilder.durable(EMAIL_DLQ).build();
  }

  /** Serializa/desserializa as tarefas como JSON (em vez da serializacao Java padrao). */
  @Bean
  public Jackson2JsonMessageConverter rabbitJsonConverter() {
    return new Jackson2JsonMessageConverter();
  }
}
