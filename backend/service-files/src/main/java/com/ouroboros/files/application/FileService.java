package com.ouroboros.files.application;

import com.ouroboros.files.adapter.out.persistence.FileMetadataRepository;
import com.ouroboros.files.domain.FileMetadata;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Casos de uso de arquivos. Blob no storage (S3) e metadados em Postgres, escopados por usuario.
 */
@Service
public class FileService {

  private final FileMetadataRepository metadata;
  private final FileStorage storage;

  public FileService(FileMetadataRepository metadata, FileStorage storage) {
    this.metadata = metadata;
    this.storage = storage;
  }

  /** Grava o blob no storage e persiste os metadados. */
  public FileMetadata store(UUID userId, String filename, String contentType, byte[] content) {
    String key = userId + "/" + UUID.randomUUID();
    storage.put(key, content, contentType);
    return metadata.save(FileMetadata.create(userId, filename, contentType, content.length, key));
  }

  public List<FileMetadata> list(UUID userId) {
    return metadata.findByUserIdOrderByCreatedAtDesc(userId);
  }

  /** Baixa o conteudo do arquivo do usuario. */
  public FileDownload download(UUID userId, UUID id) {
    FileMetadata meta =
        metadata.findByIdAndUserId(id, userId).orElseThrow(() -> new FileNotFoundException(id));
    return new FileDownload(meta, storage.get(meta.getStorageKey()));
  }

  /** Remove o blob do storage e os metadados. */
  public void delete(UUID userId, UUID id) {
    FileMetadata meta =
        metadata.findByIdAndUserId(id, userId).orElseThrow(() -> new FileNotFoundException(id));
    storage.delete(meta.getStorageKey());
    metadata.delete(meta);
  }
}
