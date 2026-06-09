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
    String normalized = normalize(name);
    requireUniqueName(userId, normalized, null);
    return categories.save(Category.create(userId, normalized, color));
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
    String normalized = normalize(name);
    requireUniqueName(userId, normalized, id);
    category.update(normalized, color);
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

  /**
   * Remove espacos nas pontas para que nomes como " Mercado" e "Mercado " nao escapem da regra de
   * unicidade nem sujem os dados (espelha {@code AuthService.normalizeEmail}). O {@code @NotBlank}
   * do request garante que o resultado nao fica vazio.
   */
  private static String normalize(String name) {
    return name == null ? null : name.trim();
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
