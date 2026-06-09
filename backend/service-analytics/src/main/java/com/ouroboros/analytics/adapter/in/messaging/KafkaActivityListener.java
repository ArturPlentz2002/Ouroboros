package com.ouroboros.analytics.adapter.in.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ouroboros.analytics.application.AnalyticsService;
import com.ouroboros.shared.events.AgendaEventCreatedEvent;
import com.ouroboros.shared.events.FinanceEntryEvent;
import com.ouroboros.shared.events.Topics;
import com.ouroboros.shared.events.UserRegisteredEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consome os eventos de dominio do Kafka e registra a atividade do usuario. O {@code eventType}
 * gravado e o nome do topico; a gravacao e idempotente (mesma chave reescreve a mesma linha).
 */
@Component
public class KafkaActivityListener {

  private final AnalyticsService analytics;
  private final ObjectMapper mapper;

  public KafkaActivityListener(AnalyticsService analytics, ObjectMapper mapper) {
    this.analytics = analytics;
    this.mapper = mapper;
  }

  @KafkaListener(topics = Topics.USER_REGISTERED)
  public void onUserRegistered(String payload) throws JsonProcessingException {
    UserRegisteredEvent event = mapper.readValue(payload, UserRegisteredEvent.class);
    analytics.record(
        event.userId(),
        event.eventId(),
        Topics.USER_REGISTERED,
        "service-user",
        event.occurredAt());
  }

  @KafkaListener(topics = Topics.AGENDA_EVENT_CREATED)
  public void onAgendaEventCreated(String payload) throws JsonProcessingException {
    AgendaEventCreatedEvent event = mapper.readValue(payload, AgendaEventCreatedEvent.class);
    analytics.record(
        event.userId(),
        event.eventId(),
        Topics.AGENDA_EVENT_CREATED,
        "service-agenda",
        event.occurredAt());
  }

  @KafkaListener(
      topics = {
        Topics.FINANCE_ENTRY_CREATED,
        Topics.FINANCE_ENTRY_UPDATED,
        Topics.FINANCE_ENTRY_DELETED
      })
  public void onFinanceEntryChanged(
      String payload,
      @org.springframework.messaging.handler.annotation.Header(
              org.springframework.kafka.support.KafkaHeaders.RECEIVED_TOPIC)
          String topic)
      throws JsonProcessingException {
    FinanceEntryEvent event = mapper.readValue(payload, FinanceEntryEvent.class);
    analytics.record(event.userId(), event.eventId(), topic, "service-finance", event.occurredAt());
  }
}
