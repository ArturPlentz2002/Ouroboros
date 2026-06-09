package com.ouroboros.files.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.ouroboros.files.domain.FileMetadata;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * IT de persistencia: exercita o {@link FileMetadataRepository} contra um Postgres real,
 * complementando o IT de API ({@code FileIT}). Foca em ordenacao e escopo por usuario.
 */
@SpringBootTest
@Testcontainers
class FileMetadataRepositoryIT {

  private static final UUID USER = UUID.randomUUID();
  private static final UUID OTHER = UUID.randomUUID();

  @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private FileMetadataRepository repository;

  private FileMetadata save(UUID userId, String filename, String key) {
    return repository.save(FileMetadata.create(userId, filename, "text/plain", 3, key));
  }

  @Test
  void listaDoUsuarioOrdenadaPorCriacaoDesc() throws InterruptedException {
    repository.deleteAll();
    save(USER, "antigo.txt", USER + "/k1");
    Thread.sleep(20); // garante createdAt distinto (precisao de milissegundos)
    save(USER, "recente.txt", USER + "/k2");
    save(OTHER, "alheio.txt", OTHER + "/k3");

    List<FileMetadata> result = repository.findByUserIdOrderByCreatedAtDesc(USER);

    assertThat(result)
        .extracting(FileMetadata::getFilename)
        .containsExactly("recente.txt", "antigo.txt");
  }

  @Test
  void findByIdAndUserIdEscopaPorDono() {
    repository.deleteAll();
    FileMetadata meta = save(USER, "privado.txt", USER + "/k");

    assertThat(repository.findByIdAndUserId(meta.getId(), USER)).isPresent();
    assertThat(repository.findByIdAndUserId(meta.getId(), OTHER)).isEmpty();
  }
}
