package com.ouroboros.user.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Payload de atualizacao de perfil. */
public record UpdateProfileRequest(
    @NotBlank @Size(max = 100) String displayName, @Size(max = 500) String avatarUrl) {}
