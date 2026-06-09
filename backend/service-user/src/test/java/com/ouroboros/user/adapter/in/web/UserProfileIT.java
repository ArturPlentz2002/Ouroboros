package com.ouroboros.user.adapter.in.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UserProfileIT {

  private static final String ME = "/api/v1/users/me";

  @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @DynamicPropertySource
  static void datasource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private MockMvc mvc;

  private static RequestPostProcessor asUser(UUID userId, String email) {
    return jwt().jwt(builder -> builder.subject(userId.toString()).claim("email", email));
  }

  @Test
  void semTokenRetorna401() throws Exception {
    mvc.perform(get(ME)).andExpect(status().isUnauthorized());
  }

  @Test
  void getMeCriaPerfilLazyDoJwt() throws Exception {
    UUID userId = UUID.randomUUID();

    mvc.perform(get(ME).with(asUser(userId, "ana@ouroboros.dev")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(userId.toString()))
        .andExpect(jsonPath("$.email").value("ana@ouroboros.dev"))
        .andExpect(jsonPath("$.displayName").value("ana"));
  }

  @Test
  void putMeAtualizaPerfil() throws Exception {
    UUID userId = UUID.randomUUID();

    mvc.perform(
            put(ME)
                .with(asUser(userId, "bia@ouroboros.dev"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\":\"Bia Souza\",\"avatarUrl\":\"https://x/b.png\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.displayName").value("Bia Souza"))
        .andExpect(jsonPath("$.avatarUrl").value("https://x/b.png"));

    mvc.perform(get(ME).with(asUser(userId, "bia@ouroboros.dev")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.displayName").value("Bia Souza"));
  }
}
