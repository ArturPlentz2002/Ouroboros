package com.ouroboros.analytics.adapter.in.messaging;

import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ouroboros.analytics.application.AnalyticsService;
import com.ouroboros.shared.events.AgendaEventCreatedEvent;
import com.ouroboros.shared.events.FinanceChangeType;
import com.ouroboros.shared.events.FinanceEntryEvent;
import com.ouroboros.shared.events.Topics;
import com.ouroboros.shared.events.UserRegisteredEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KafkaActivityListenerTest {

  private final JsonMapper mapper =
      JsonMapper.builder()
          .addModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
          .build();

  @Mock private AnalyticsService analytics;

  @Test
  void usuarioRegistradoGeraAtividade() throws Exception {
    KafkaActivityListener listener = new KafkaActivityListener(analytics, mapper);
    UUID user = UUID.randomUUID();
    Instant t = Instant.parse("2026-01-01T10:00:00Z");
    var event = new UserRegisteredEvent("ev-1", user, "a@b.com", t);

    listener.onUserRegistered(mapper.writeValueAsString(event));

    verify(analytics).record(user, "ev-1", Topics.USER_REGISTERED, "service-user", t);
  }

  @Test
  void eventoDaAgendaGeraAtividade() throws Exception {
    KafkaActivityListener listener = new KafkaActivityListener(analytics, mapper);
    UUID user = UUID.randomUUID();
    Instant t = Instant.parse("2026-02-01T10:00:00Z");
    var event = new AgendaEventCreatedEvent("ev-2", user, UUID.randomUUID(), "Reuniao", t, t);

    listener.onAgendaEventCreated(mapper.writeValueAsString(event));

    verify(analytics).record(user, "ev-2", Topics.AGENDA_EVENT_CREATED, "service-agenda", t);
  }

  @Test
  void mudancaFinanceiraUsaOTopicoComoTipo() throws Exception {
    KafkaActivityListener listener = new KafkaActivityListener(analytics, mapper);
    UUID user = UUID.randomUUID();
    Instant t = Instant.parse("2026-03-01T10:00:00Z");
    var event =
        new FinanceEntryEvent(
            "ev-3",
            user,
            UUID.randomUUID(),
            FinanceChangeType.UPDATED,
            "EXPENSE",
            new BigDecimal("10.00"),
            t);

    listener.onFinanceEntryChanged(mapper.writeValueAsString(event), Topics.FINANCE_ENTRY_UPDATED);

    verify(analytics).record(user, "ev-3", Topics.FINANCE_ENTRY_UPDATED, "service-finance", t);
  }
}
