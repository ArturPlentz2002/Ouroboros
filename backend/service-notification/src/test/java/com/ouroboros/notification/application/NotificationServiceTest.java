package com.ouroboros.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ouroboros.notification.adapter.out.messaging.EmailTaskPublisher;
import com.ouroboros.notification.adapter.out.persistence.NotificationRepository;
import com.ouroboros.notification.adapter.out.persistence.ProcessedEventRepository;
import com.ouroboros.notification.domain.Notification;
import com.ouroboros.notification.domain.ProcessedEvent;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock private NotificationRepository notifications;
  @Mock private ProcessedEventRepository processed;
  @Mock private EmailTaskPublisher emailPublisher;
  @InjectMocks private NotificationService service;

  @Test
  void recordCriaNotificacaoSemEnfileirarNaTransacao() {
    UUID eventId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    when(processed.existsById(eventId)).thenReturn(false);
    when(notifications.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

    Optional<Notification> created =
        service.record(eventId, userId, "USER_REGISTERED", "Bem-vindo", "ola");

    assertThat(created).isPresent();
    verify(notifications).save(any(Notification.class));
    verify(processed).save(any(ProcessedEvent.class));
    // O e-mail NAO e enfileirado dentro de record (so apos o commit, via enqueueEmail).
    verifyNoInteractions(emailPublisher);
  }

  @Test
  void recordIdempotenteIgnoraEventoJaProcessado() {
    UUID eventId = UUID.randomUUID();
    when(processed.existsById(eventId)).thenReturn(true);

    Optional<Notification> created =
        service.record(eventId, UUID.randomUUID(), "USER_REGISTERED", "Bem-vindo", "ola");

    assertThat(created).isEmpty();
    verify(notifications, never()).save(any(Notification.class));
    verify(processed, never()).save(any(ProcessedEvent.class));
    verifyNoInteractions(emailPublisher);
  }

  @Test
  void enqueueEmailPublicaTarefa() {
    Notification notification = Notification.create(UUID.randomUUID(), "USER_REGISTERED", "t", "m");

    service.enqueueEmail(notification);

    verify(emailPublisher).enqueue(any(EmailTask.class));
  }

  @Test
  void markEmailedMarcaQuandoExiste() {
    UUID id = UUID.randomUUID();
    Notification notification = Notification.create(UUID.randomUUID(), "USER_REGISTERED", "t", "m");
    when(notifications.findById(id)).thenReturn(Optional.of(notification));

    service.markEmailed(id);

    assertThat(notification.getEmailedAt()).isNotNull();
  }
}
