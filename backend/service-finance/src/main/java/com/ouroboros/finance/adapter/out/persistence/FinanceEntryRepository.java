package com.ouroboros.finance.adapter.out.persistence;

import com.ouroboros.finance.domain.FinanceEntry;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinanceEntryRepository extends JpaRepository<FinanceEntry, UUID> {

  List<FinanceEntry> findByUserIdOrderByOccurredOnDescCreatedAtDesc(UUID userId);

  Optional<FinanceEntry> findByIdAndUserId(UUID id, UUID userId);
}
