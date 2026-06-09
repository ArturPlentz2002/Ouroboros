package com.ouroboros.files.adapter.in.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class FileIT {

  private static final String BASE = "/api/v1/files";
  private static final String BUCKET = "ouroboros-files";

  @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @Container
  static LocalStackContainer localstack =
      new LocalStackContainer(DockerImageName.parse("localstack/localstack:3"))
          .withServices(Service.S3);

  @BeforeAll
  static void createBucket() throws Exception {
    localstack.execInContainer("awslocal", "s3", "mb", "s3://" + BUCKET);
  }

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add(
        "ouroboros.s3.endpoint", () -> localstack.getEndpointOverride(Service.S3).toString());
    registry.add("ouroboros.s3.region", localstack::getRegion);
    registry.add("ouroboros.s3.access-key", localstack::getAccessKey);
    registry.add("ouroboros.s3.secret-key", localstack::getSecretKey);
    registry.add("ouroboros.s3.bucket", () -> BUCKET);
  }

  @Autowired private MockMvc mvc;

  private static RequestPostProcessor asUser(UUID userId) {
    return jwt().jwt(builder -> builder.subject(userId.toString()));
  }

  private static MockMultipartFile file(String name) {
    return new MockMultipartFile(
        "file", name, "text/plain", "conteudo do arquivo".getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void semTokenRetorna401() throws Exception {
    mvc.perform(get(BASE)).andExpect(status().isUnauthorized());
  }

  @Test
  void uploadEListaEscopadoPorUsuario() throws Exception {
    UUID userA = UUID.randomUUID();
    UUID userB = UUID.randomUUID();

    mvc.perform(multipart(BASE).file(file("a.txt")).with(asUser(userA)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.filename").value("a.txt"))
        .andExpect(jsonPath("$.contentType").value("text/plain"))
        .andExpect(jsonPath("$.size").value(19));

    mvc.perform(get(BASE).with(asUser(userA)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].filename").value("a.txt"));

    mvc.perform(get(BASE).with(asUser(userB)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }
}
