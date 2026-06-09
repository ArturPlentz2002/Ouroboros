package com.ouroboros.finance.adapter.out.messaging;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Publica no Kafka os eventos pendentes do outbox e os marca como enviados. Entrega at-least-once:
 * so marca {@code sentAt} apos o broker confirmar; uma falha apos publicar reenvia (o consumidor
 * deduplica pelo id do evento). Pode ser desligado (ex.: testes sem broker) via {@code
 * ouroboros.outbox.relay.enabled=false}.
 */
@Component
@ConditionalOnProperty(
    value = "ouroboros.outbox.relay.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class OutboxRelay {

  private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

  private final OutboxEventRepository outbox;
  private final KafkaTemplate<String, String> kafka;

  public OutboxRelay(OutboxEventRepository outbox, KafkaTemplate<String, String> kafka) {
    this.outbox = outbox;
    this.kafka = kafka;
  }

  @Scheduled(fixedDelayString = "${ouroboros.outbox.poll-interval-ms:2000}")
  @Transactional
  public void publishPending() {
    List<OutboxEvent> pending = outbox.findTop100BySentAtIsNullOrderByCreatedAtAsc();
    for (OutboxEvent event : pending) {
      try {
        kafka
            .send(event.getTopic(), event.getAggregateId().toString(), event.getPayload())
            .get(10, TimeUnit.SECONDS);
        event.markSent(Instant.now());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      } catch (Exception e) {
        // Deixa sentAt nulo: sera reenviado no proximo ciclo.
        log.warn("Falha ao publicar evento de outbox {} ({})", event.getId(), event.getTopic(), e);
      }
    }
  }
}
