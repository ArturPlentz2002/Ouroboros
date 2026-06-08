package com.ouroboros.auth.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

/** Payload de login social: provedor + ID token emitido pelo provedor. */
public record SocialLoginRequest(@NotBlank String provider, @NotBlank String idToken) {}
