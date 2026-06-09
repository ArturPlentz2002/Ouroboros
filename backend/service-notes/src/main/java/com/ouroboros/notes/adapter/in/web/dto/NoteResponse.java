package com.ouroboros.notes.adapter.in.web.dto;

import com.ouroboros.notes.domain.Note;
import java.time.Instant;
import java.util.List;

/** Representacao de uma nota na API. */
public record NoteResponse(
    String id,
    String title,
    String content,
    List<String> tags,
    Instant createdAt,
    Instant updatedAt) {

  public static NoteResponse from(Note note) {
    return new NoteResponse(
        note.getId(),
        note.getTitle(),
        note.getContent(),
        note.getTags(),
        note.getCreatedAt(),
        note.getUpdatedAt());
  }
}
