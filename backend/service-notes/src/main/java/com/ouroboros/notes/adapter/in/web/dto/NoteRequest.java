package com.ouroboros.notes.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/** Payload de criacao/atualizacao de nota. */
public record NoteRequest(@NotBlank String title, String content, List<String> tags) {}
