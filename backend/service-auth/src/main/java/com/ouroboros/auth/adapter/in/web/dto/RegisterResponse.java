package com.ouroboros.auth.adapter.in.web.dto;

import java.util.UUID;

/** Resposta do registro de usuario. */
public record RegisterResponse(UUID id, String email) {}
