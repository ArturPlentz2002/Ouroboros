package com.ouroboros.agenda.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ouroboros.agenda.adapter.out.messaging.OutboxWriter;
import com.ouroboros.agenda.adapter.out.persistence.AgendaEventRepository;
import com.ouroboros.agenda.domain.AgendaEvent;
import com.ouroboros.agenda.domain.InvalidEventTimeException;
import com.ouroboros.shared.events.AgendaEventCreatedEvent;
import com.ouroboros.shared.events.Topics;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgendaServiceTest {

  private static final Instant START = Instant.parse("2026-07-01T10:00:00Z");
  private static final Instant END = START.plus(Duration.ofHours(1));

  @Mock private AgendaEventRepository events;
  @Mock private OutboxWriter outbox;
  private AgendaService service;

  @org.junit.jupiter.api.BeforeEach
  void setUp() {
    service = new AgendaService(events, outbox);
  }

  @Test
  void createSalvaEventoEscopadoNoUsuario() {
    UUID userId = UUID.randomUUID();
    when(events.save(any(AgendaEvent.class))).thenAnswer(inv -> inv.getArgument(0));

    AgendaEvent event = service.create(userId, "Reuniao", "pauta", START, END);

    assertThat(event.getUserId()).isEqualTo(userId);
    assertThat(event.getTitle()).isEqualTo("Reuniao");
    verify(events).save(any(AgendaEvent.class));
    verify(outbox)
        .write(
            any(),
            eq(Topics.AGENDA_EVENT_CREATED),
            eq("AgendaEvent"),
            eq(event.getId()),
            any(AgendaEventCreatedEvent.class));
  }

  @Test
  void createComTempoInvalidoNaoSalva() {
    UUID userId = UUID.randomUUID();

    assertThatThrownBy(() -> service.create(userId, "Reuniao", null, START, START.minusSeconds(1)))
        .isInstanceOf(InvalidEventTimeException.class);

    verify(events, never()).save(any());
  }

  @Test
  void getInexistenteLanca() {
    UUID userId = UUID.randomUUID();
    UUID id = UUID.randomUUID();
    when(events.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.get(userId, id)).isInstanceOf(EventNotFoundException.class);
  }

  @Test
  void deleteRemoveQuandoPertenceAoUsuario() {
    UUID userId = UUID.randomUUID();
    AgendaEvent event = AgendaEvent.create(userId, "Reuniao", null, START, END);
    when(events.findByIdAndUserId(event.getId(), userId)).thenReturn(Optional.of(event));

    service.delete(userId, event.getId());

    verify(events).delete(event);
  }

  @Test
  void updateInexistenteLanca() {
    UUID userId = UUID.randomUUID();
    UUID id = UUID.randomUUID();
    when(events.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.update(userId, id, "novo", null, START, END))
        .isInstanceOf(EventNotFoundException.class);
  }
}
