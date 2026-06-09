package com.ouroboros.files.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Metadados de um arquivo armazenado no S3, sempre pertencente a um usuario. */
@Entity
@Table(name = "file_metadata")
public class FileMetadata {

  @Id private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(nullable = false)
  private String filename;

  @Column(name = "content_type")
  private String contentType;

  @Column(nullable = false)
  private long size;

  @Column(name = "storage_key", nullable = false)
  private String storageKey;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected FileMetadata() {
    // exigido pelo JPA
  }

  private FileMetadata(
      UUID id,
      UUID userId,
      String filename,
      String contentType,
      long size,
      String storageKey,
      Instant createdAt) {
    this.id = id;
    this.userId = userId;
    this.filename = filename;
    this.contentType = contentType;
    this.size = size;
    this.storageKey = storageKey;
    this.createdAt = createdAt;
  }

  /** Cria os metadados de um novo arquivo. A chave de storage e gerada pelo chamador. */
  public static FileMetadata create(
      UUID userId, String filename, String contentType, long size, String storageKey) {
    return new FileMetadata(
        UUID.randomUUID(), userId, filename, contentType, size, storageKey, Instant.now());
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getFilename() {
    return filename;
  }

  public String getContentType() {
    return contentType;
  }

  public long getSize() {
    return size;
  }

  public String getStorageKey() {
    return storageKey;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
