package com.ouroboros.auth.application;

import java.util.UUID;

/** Resultado do registro de um usuario. */
public record RegisteredUser(UUID id, String email) {}
