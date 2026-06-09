package com.ouroboros.auth.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Payload de login. */
public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
