package com.ouroboros.finance.adapter.out.persistence;

import com.ouroboros.finance.domain.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

  List<Category> findByUserIdOrderByNameAsc(UUID userId);

  Optional<Category> findByIdAndUserId(UUID id, UUID userId);

  Optional<Category> findByUserIdAndNameIgnoreCase(UUID userId, String name);
}
