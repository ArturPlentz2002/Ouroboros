package com.ouroboros.agenda.adapter.out.persistence;

import com.ouroboros.agenda.domain.AgendaEvent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgendaEventRepository extends JpaRepository<AgendaEvent, UUID> {

  List<AgendaEvent> findByUserIdOrderByStartsAtAsc(UUID userId);

  Optional<AgendaEvent> findByIdAndUserId(UUID id, UUID userId);
}
