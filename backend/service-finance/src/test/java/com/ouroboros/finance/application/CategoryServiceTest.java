package com.ouroboros.finance.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ouroboros.finance.adapter.out.persistence.CategoryRepository;
import com.ouroboros.finance.domain.Category;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock private CategoryRepository categories;
  @InjectMocks private CategoryService service;

  private final UUID userId = UUID.randomUUID();

  @Test
  void criaQuandoNomeEhUnico() {
    when(categories.findByUserIdAndNameIgnoreCase(userId, "Mercado")).thenReturn(Optional.empty());
    when(categories.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

    Category created = service.create(userId, "Mercado", "#4caf50");

    assertThat(created.getName()).isEqualTo("Mercado");
    assertThat(created.getColor()).isEqualTo("#4caf50");
    assertThat(created.getUserId()).isEqualTo(userId);
    verify(categories).save(any(Category.class));
  }

  @Test
  void rejeitaNomeDuplicadoNaCriacao() {
    Category existing = Category.create(userId, "Mercado", null);
    when(categories.findByUserIdAndNameIgnoreCase(userId, "Mercado"))
        .thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> service.create(userId, "Mercado", null))
        .isInstanceOf(DuplicateCategoryException.class);
    verify(categories, never()).save(any(Category.class));
  }

  @Test
  void getInexistenteLancaNotFound() {
    UUID id = UUID.randomUUID();
    when(categories.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.get(userId, id)).isInstanceOf(CategoryNotFoundException.class);
  }

  @Test
  void atualizaPermitindoManterOProprioNome() {
    Category category = Category.create(userId, "Mercado", null);
    UUID id = category.getId();
    when(categories.findByIdAndUserId(id, userId)).thenReturn(Optional.of(category));
    when(categories.findByUserIdAndNameIgnoreCase(userId, "Mercado"))
        .thenReturn(Optional.of(category));
    when(categories.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

    Category updated = service.update(userId, id, "Mercado", "#000000");

    assertThat(updated.getColor()).isEqualTo("#000000");
    verify(categories).save(category);
  }

  @Test
  void rejeitaRenomearParaNomeDeOutraCategoria() {
    Category target = Category.create(userId, "Mercado", null);
    Category other = Category.create(userId, "Lazer", null);
    UUID id = target.getId();
    when(categories.findByIdAndUserId(id, userId)).thenReturn(Optional.of(target));
    when(categories.findByUserIdAndNameIgnoreCase(userId, "Lazer")).thenReturn(Optional.of(other));

    assertThatThrownBy(() -> service.update(userId, id, "Lazer", null))
        .isInstanceOf(DuplicateCategoryException.class);
    verify(categories, never()).save(any(Category.class));
  }

  @Test
  void deletaInexistenteLancaNotFound() {
    UUID id = UUID.randomUUID();
    when(categories.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.delete(userId, id))
        .isInstanceOf(CategoryNotFoundException.class);
    verify(categories, never()).delete(any(Category.class));
  }
}
