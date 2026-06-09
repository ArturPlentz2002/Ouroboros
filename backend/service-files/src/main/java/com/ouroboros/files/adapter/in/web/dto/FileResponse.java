package com.ouroboros.files.adapter.in.web.dto;

import com.ouroboros.files.domain.FileMetadata;
import java.time.Instant;
import java.util.UUID;

/** Representacao dos metadados de um arquivo na API. */
public record FileResponse(
    UUID id, String filename, String contentType, long size, Instant createdAt) {

  public static FileResponse from(FileMetadata meta) {
    return new FileResponse(
        meta.getId(),
        meta.getFilename(),
        meta.getContentType(),
        meta.getSize(),
        meta.getCreatedAt());
  }
}
