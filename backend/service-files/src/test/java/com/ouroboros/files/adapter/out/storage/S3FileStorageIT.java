package com.ouroboros.files.adapter.out.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ouroboros.files.config.S3Config;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

/** IT do adapter de armazenamento: round-trip put/get/delete contra um S3 real (LocalStack). */
@Testcontainers
class S3FileStorageIT {

  private static final String BUCKET = "ouroboros-files-it";

  @Container
  static LocalStackContainer localstack =
      new LocalStackContainer(DockerImageName.parse("localstack/localstack:3"))
          .withServices(Service.S3);

  private static S3FileStorage storage;

  @BeforeAll
  static void setup() throws Exception {
    localstack.execInContainer("awslocal", "s3", "mb", "s3://" + BUCKET);
    var s3 =
        new S3Config()
            .s3Client(
                localstack.getRegion(),
                localstack.getEndpointOverride(Service.S3).toString(),
                localstack.getAccessKey(),
                localstack.getSecretKey());
    storage = new S3FileStorage(s3, BUCKET);
  }

  @Test
  void putThenGetDevolveOMesmoConteudo() {
    byte[] content = "conteudo binario".getBytes(StandardCharsets.UTF_8);

    storage.put("chave/1", content, "text/plain");

    assertThat(storage.get("chave/1")).isEqualTo(content);
  }

  @Test
  void deleteRemoveOObjeto() {
    byte[] content = "temp".getBytes(StandardCharsets.UTF_8);
    storage.put("chave/2", content, "text/plain");

    storage.delete("chave/2");

    assertThatThrownBy(() -> storage.get("chave/2")).isInstanceOf(NoSuchKeyException.class);
  }
}
