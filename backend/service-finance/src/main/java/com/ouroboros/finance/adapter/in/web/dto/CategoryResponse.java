package com.ouroboros.finance.adapter.in.web.dto;

import com.ouroboros.finance.domain.Category;
import java.time.Instant;
import java.util.UUID;

/** Representacao de uma categoria na API. */
public record CategoryResponse(UUID id, String name, String color, Instant createdAt) {

  public static CategoryResponse from(Category category) {
    return new CategoryResponse(
        category.getId(), category.getName(), category.getColor(), category.getCreatedAt());
  }
}
