package com.ouroboros.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Tratamento de erro do consumidor Kafka: tenta novamente algumas vezes e, persistindo a falha,
 * encaminha a mensagem para o topico de dead-letter ({@code <topico>-dlt}).
 */
@Configuration
public class KafkaConsumerConfig {

  @Bean
  public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> template) {
    DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template);
    // 2 retentativas com 1s de intervalo antes de mandar para a DLT.
    return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2L));
  }
}
