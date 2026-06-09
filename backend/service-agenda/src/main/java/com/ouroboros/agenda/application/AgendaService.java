package com.ouroboros.agenda.application;

import com.ouroboros.agenda.adapter.out.messaging.OutboxWriter;
import com.ouroboros.agenda.adapter.out.persistence.AgendaEventRepository;
import com.ouroboros.agenda.domain.AgendaEvent;
import com.ouroboros.shared.events.AgendaEventCreatedEvent;
import com.ouroboros.shared.events.Topics;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Casos de uso da agenda. Todas as operacoes sao escopadas pelo usuario dono. */
@Service
public class AgendaService {

  private static final String AGGREGATE = "AgendaEvent";

  private final AgendaEventRepository events;
  private final OutboxWriter outbox;

  public AgendaService(AgendaEventRepository events, OutboxWriter outbox) {
    this.events = events;
    this.outbox = outbox;
  }

  @Transactional
  public AgendaEvent create(
      UUID userId, String title, String description, Instant startsAt, Instant endsAt) {
    AgendaEvent event = AgendaEvent.create(userId, title, description, startsAt, endsAt);
    AgendaEvent saved = events.save(event);
    publishCreated(saved);
    return saved;
  }

  @Transactional(readOnly = true)
  public List<AgendaEvent> list(UUID userId) {
    return events.findByUserIdOrderByStartsAtAsc(userId);
  }

  @Transactional(readOnly = true)
  public AgendaEvent get(UUID userId, UUID id) {
    return events.findByIdAndUserId(id, userId).orElseThrow(() -> new EventNotFoundException(id));
  }

  @Transactional
  public AgendaEvent update(
      UUID userId, UUID id, String title, String description, Instant startsAt, Instant endsAt) {
    AgendaEvent event =
        events.findByIdAndUserId(id, userId).orElseThrow(() -> new EventNotFoundException(id));
    event.update(title, description, startsAt, endsAt);
    return events.save(event);
  }

  @Transactional
  public void delete(UUID userId, UUID id) {
    AgendaEvent event =
        events.findByIdAndUserId(id, userId).orElseThrow(() -> new EventNotFoundException(id));
    events.delete(event);
  }

  /** Enfileira o evento agenda.event.created no outbox (mesma transacao da criacao). */
  private void publishCreated(AgendaEvent event) {
    UUID eventId = UUID.randomUUID();
    AgendaEventCreatedEvent payload =
        new AgendaEventCreatedEvent(
            eventId.toString(),
            event.getUserId(),
            event.getId(),
            event.getTitle(),
            event.getStartsAt(),
            Instant.now());
    outbox.write(eventId, Topics.AGENDA_EVENT_CREATED, AGGREGATE, event.getId(), payload);
  }
}
