package com.ouroboros.auth.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

/** Payload de refresh de token. */
public record RefreshRequest(@NotBlank String refreshToken) {}
