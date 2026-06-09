package com.ouroboros.notification.adapter.in.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ouroboros.notification.application.NotificationService;
import com.ouroboros.shared.events.AgendaEventCreatedEvent;
import com.ouroboros.shared.events.FinanceEntryEvent;
import com.ouroboros.shared.events.Topics;
import com.ouroboros.shared.events.UserRegisteredEvent;
import java.util.UUID;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consome os eventos de dominio do Kafka e gera notificacoes. O group-id ({@code
 * service-notification}) vem do application.yml. Erros vao para a DLT via {@code
 * KafkaConsumerConfig}.
 */
@Component
public class KafkaEventListener {

  private final NotificationService service;
  private final ObjectMapper mapper;

  public KafkaEventListener(NotificationService service, ObjectMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @KafkaListener(topics = Topics.USER_REGISTERED)
  public void onUserRegistered(String payload) throws JsonProcessingException {
    UserRegisteredEvent event = mapper.readValue(payload, UserRegisteredEvent.class);
    service
        .record(
            UUID.fromString(event.eventId()),
            event.userId(),
            "USER_REGISTERED",
            "Bem-vindo ao Ouroboros!",
            "Sua conta " + event.email() + " foi criada.")
        .ifPresent(service::enqueueEmail);
  }

  @KafkaListener(topics = Topics.AGENDA_EVENT_CREATED)
  public void onAgendaEventCreated(String payload) throws JsonProcessingException {
    AgendaEventCreatedEvent event = mapper.readValue(payload, AgendaEventCreatedEvent.class);
    service
        .record(
            UUID.fromString(event.eventId()),
            event.userId(),
            "AGENDA_EVENT_CREATED",
            "Novo evento na agenda",
            event.title())
        .ifPresent(service::enqueueEmail);
  }

  @KafkaListener(topics = Topics.FINANCE_ENTRY_CREATED)
  public void onFinanceEntryCreated(String payload) throws JsonProcessingException {
    FinanceEntryEvent event = mapper.readValue(payload, FinanceEntryEvent.class);
    service
        .record(
            UUID.fromString(event.eventId()),
            event.userId(),
            "FINANCE_ENTRY_CREATED",
            "Lancamento registrado",
            event.type() + " de " + event.amount())
        .ifPresent(service::enqueueEmail);
  }
}
