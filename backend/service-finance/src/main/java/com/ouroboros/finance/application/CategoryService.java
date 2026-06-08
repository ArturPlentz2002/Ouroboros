package com.ouroboros.finance.application;

import com.ouroboros.finance.adapter.out.persistence.CategoryRepository;
import com.ouroboros.finance.domain.Category;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Casos de uso de categorias. Todas as operacoes sao escopadas pelo usuario dono. */
@Service
public class CategoryService {

  private final CategoryRepository categories;

  public CategoryService(CategoryRepository categories) {
    this.categories = categories;
  }

  @Transactional
  public Category create(UUID userId, String name, String color) {
    requireUniqueName(userId, name, null);
    return categories.save(Category.create(userId, name, color));
  }

  @Transactional(readOnly = true)
  public List<Category> list(UUID userId) {
    return categories.findByUserIdOrderByNameAsc(userId);
  }

  @Transactional(readOnly = true)
  public Category get(UUID userId, UUID id) {
    return categories
        .findByIdAndUserId(id, userId)
        .orElseThrow(() -> new CategoryNotFoundException(id));
  }

  @Transactional
  public Category update(UUID userId, UUID id, String name, String color) {
    Category category =
        categories
            .findByIdAndUserId(id, userId)
            .orElseThrow(() -> new CategoryNotFoundException(id));
    requireUniqueName(userId, name, id);
    category.update(name, color);
    return categories.save(category);
  }

  @Transactional
  public void delete(UUID userId, UUID id) {
    Category category =
        categories
            .findByIdAndUserId(id, userId)
            .orElseThrow(() -> new CategoryNotFoundException(id));
    categories.delete(category);
  }

  /** Garante que o nome nao colide com outra categoria do mesmo usuario. */
  private void requireUniqueName(UUID userId, String name, UUID selfId) {
    categories
        .findByUserIdAndNameIgnoreCase(userId, name)
        .filter(existing -> !existing.getId().equals(selfId))
        .ifPresent(
            existing -> {
              throw new DuplicateCategoryException(name);
            });
  }
}
