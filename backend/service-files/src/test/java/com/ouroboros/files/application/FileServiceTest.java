package com.ouroboros.files.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ouroboros.files.adapter.out.persistence.FileMetadataRepository;
import com.ouroboros.files.domain.FileMetadata;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

  private static final UUID USER = UUID.randomUUID();

  @Mock private FileMetadataRepository metadata;
  @Mock private FileStorage storage;
  @InjectMocks private FileService service;

  @Test
  void storeGravaBlobEPersisteMetadados() {
    when(metadata.save(any(FileMetadata.class))).thenAnswer(inv -> inv.getArgument(0));
    byte[] content = "conteudo".getBytes(StandardCharsets.UTF_8);

    FileMetadata saved = service.store(USER, "a.txt", "text/plain", content);

    assertThat(saved.getUserId()).isEqualTo(USER);
    assertThat(saved.getFilename()).isEqualTo("a.txt");
    assertThat(saved.getContentType()).isEqualTo("text/plain");
    assertThat(saved.getSize()).isEqualTo(content.length);
    assertThat(saved.getStorageKey()).startsWith(USER + "/");
    verify(storage).put(eq(saved.getStorageKey()), eq(content), eq("text/plain"));
    verify(metadata).save(any(FileMetadata.class));
  }

  @Test
  void listDelegaAoRepositorioEscopadoPorUsuario() {
    FileMetadata m = FileMetadata.create(USER, "a.txt", "text/plain", 3, "k");
    when(metadata.findByUserIdOrderByCreatedAtDesc(USER)).thenReturn(List.of(m));

    assertThat(service.list(USER)).containsExactly(m);
  }
}
