package com.ouroboros.notification.adapter.out.persistence;

import com.ouroboros.notification.domain.ProcessedEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {}
