package com.ouroboros.finance.adapter.in.web;

import com.ouroboros.finance.adapter.in.web.dto.CategoryRequest;
import com.ouroboros.finance.adapter.in.web.dto.CategoryResponse;
import com.ouroboros.finance.application.CategoryService;
import com.ouroboros.shared.ApiPaths;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/finance/categories")
public class CategoryController {

  private final CategoryService categoryService;

  public CategoryController(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  @PostMapping
  public ResponseEntity<CategoryResponse> create(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CategoryRequest request) {
    var category = categoryService.create(userId(jwt), request.name(), request.color());
    return ResponseEntity.status(HttpStatus.CREATED).body(CategoryResponse.from(category));
  }

  @GetMapping
  public List<CategoryResponse> list(@AuthenticationPrincipal Jwt jwt) {
    return categoryService.list(userId(jwt)).stream().map(CategoryResponse::from).toList();
  }

  @GetMapping("/{id}")
  public CategoryResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    return CategoryResponse.from(categoryService.get(userId(jwt), id));
  }

  @PutMapping("/{id}")
  public CategoryResponse update(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID id,
      @Valid @RequestBody CategoryRequest request) {
    var category = categoryService.update(userId(jwt), id, request.name(), request.color());
    return CategoryResponse.from(category);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    categoryService.delete(userId(jwt), id);
    return ResponseEntity.noContent().build();
  }

  private static UUID userId(Jwt jwt) {
    return UUID.fromString(jwt.getSubject());
  }
}
