package com.ouroboros.auth.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Payload de registro de usuario. */
public record RegisterRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8, message = "A senha deve ter ao menos 8 caracteres") String password) {}
