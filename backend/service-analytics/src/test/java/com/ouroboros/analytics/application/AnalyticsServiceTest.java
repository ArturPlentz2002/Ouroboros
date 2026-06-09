package com.ouroboros.analytics.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ouroboros.analytics.adapter.out.persistence.UserActivityRepository;
import com.ouroboros.analytics.domain.UserActivity;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

  private static final UUID USER = UUID.randomUUID();

  @Mock private UserActivityRepository activities;
  @InjectMocks private AnalyticsService service;

  @Test
  void recordSalvaAtividadeComOccurredAtDoEvento() {
    when(activities.save(any(UserActivity.class))).thenAnswer(inv -> inv.getArgument(0));
    Instant t = Instant.parse("2026-01-01T10:00:00Z");

    UserActivity saved = service.record(USER, "e1", "agenda.event.created", "service-agenda", t);

    assertThat(saved.getKey().getUserId()).isEqualTo(USER);
    assertThat(saved.getKey().getEventId()).isEqualTo("e1");
    assertThat(saved.getKey().getOccurredAt()).isEqualTo(t);
    assertThat(saved.getEventType()).isEqualTo("agenda.event.created");
    assertThat(saved.getSource()).isEqualTo("service-agenda");
    verify(activities).save(any(UserActivity.class));
  }

  @Test
  void summaryContaAtividadePorTipo() {
    when(activities.findByKeyUserId(USER))
        .thenReturn(
            List.of(
                UserActivity.of(USER, "e1", "A", "s", Instant.parse("2026-01-01T00:00:00Z")),
                UserActivity.of(USER, "e2", "A", "s", Instant.parse("2026-01-02T00:00:00Z")),
                UserActivity.of(USER, "e3", "B", "s", Instant.parse("2026-01-03T00:00:00Z"))));

    Map<String, Long> summary = service.summary(USER);

    assertThat(summary).containsEntry("A", 2L).containsEntry("B", 1L);
  }

  @Test
  void listDelegaAoRepositorioEscopadoPorUsuario() {
    UserActivity a = UserActivity.of(USER, "e1", "A", "s", Instant.parse("2026-01-01T00:00:00Z"));
    when(activities.findByKeyUserId(USER)).thenReturn(List.of(a));

    assertThat(service.listActivity(USER)).containsExactly(a);
  }
}
