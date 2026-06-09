package com.ouroboros.files.adapter.out.persistence;

import com.ouroboros.files.domain.FileMetadata;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {

  List<FileMetadata> findByUserIdOrderByCreatedAtDesc(UUID userId);

  Optional<FileMetadata> findByIdAndUserId(UUID id, UUID userId);
}
