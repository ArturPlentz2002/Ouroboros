package com.ouroboros.agenda.adapter.out.messaging;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

  /** Pega um lote de eventos ainda nao publicados, mais antigos primeiro. */
  List<OutboxEvent> findTop100BySentAtIsNullOrderByCreatedAtAsc();
}
