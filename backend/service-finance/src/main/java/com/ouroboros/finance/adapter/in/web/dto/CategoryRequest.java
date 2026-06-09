package com.ouroboros.finance.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Payload de criacao/atualizacao de categoria. */
public record CategoryRequest(
    @NotBlank @Size(max = 100) String name,
    @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "deve ser uma cor hexadecimal (ex.: #4caf50)")
        String color) {}
