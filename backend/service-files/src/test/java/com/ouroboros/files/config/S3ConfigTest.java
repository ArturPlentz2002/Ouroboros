package com.ouroboros.files.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;

class S3ConfigTest {

  private final S3Config config = new S3Config();

  @Test
  void criaClienteComEndpointCustomizado() {
    try (S3Client client = config.s3Client("us-east-1", "http://localhost:4566", "test", "test")) {
      assertThat(client).isNotNull();
    }
  }

  @Test
  void criaClienteSemEndpoint() {
    try (S3Client client = config.s3Client("us-east-1", "", "test", "test")) {
      assertThat(client).isNotNull();
    }
  }
}
