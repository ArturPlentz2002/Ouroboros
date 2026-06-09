package com.ouroboros.finance.adapter.in.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
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
class CategoryIT {

  private static final String BASE = "/api/v1/finance/categories";

  @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @DynamicPropertySource
  static void datasource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper objectMapper;

  private static RequestPostProcessor asUser(UUID userId) {
    return jwt().jwt(builder -> builder.subject(userId.toString()));
  }

  private static String categoryJson(String name, String color) {
    return "{\"name\":\"" + name + "\",\"color\":\"" + color + "\"}";
  }

  private String createCategory(UUID userId, String name) throws Exception {
    String body =
        mvc.perform(
                post(BASE)
                    .with(asUser(userId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(categoryJson(name, "#4caf50")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return objectMapper.readTree(body).get("id").asText();
  }

  @Test
  void semTokenRetorna401() throws Exception {
    mvc.perform(get(BASE)).andExpect(status().isUnauthorized());
  }

  @Test
  void criaEListaEscopadoPorUsuario() throws Exception {
    UUID userA = UUID.randomUUID();
    UUID userB = UUID.randomUUID();

    createCategory(userA, "Mercado");

    mvc.perform(get(BASE).with(asUser(userA)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value("Mercado"))
        .andExpect(jsonPath("$[0].color").value("#4caf50"));

    mvc.perform(get(BASE).with(asUser(userB)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void categoriaDeOutroUsuarioRetorna404() throws Exception {
    UUID owner = UUID.randomUUID();
    UUID other = UUID.randomUUID();
    String id = createCategory(owner, "Privada");

    mvc.perform(get(BASE + "/" + id).with(asUser(other))).andExpect(status().isNotFound());
    mvc.perform(get(BASE + "/" + id).with(asUser(owner))).andExpect(status().isOk());
  }

  @Test
  void atualizaEDeleta() throws Exception {
    UUID userId = UUID.randomUUID();
    String id = createCategory(userId, "Original");

    mvc.perform(
            put(BASE + "/" + id)
                .with(asUser(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryJson("Atualizada", "#2196f3")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Atualizada"))
        .andExpect(jsonPath("$.color").value("#2196f3"));

    mvc.perform(delete(BASE + "/" + id).with(asUser(userId))).andExpect(status().isNoContent());
    mvc.perform(get(BASE + "/" + id).with(asUser(userId))).andExpect(status().isNotFound());
  }

  @Test
  void rejeitaNomeDuplicadoCom409() throws Exception {
    UUID userId = UUID.randomUUID();
    createCategory(userId, "Mercado");

    mvc.perform(
            post(BASE)
                .with(asUser(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryJson("mercado", "#000000")))
        .andExpect(status().isConflict());
  }

  @Test
  void rejeitaCorInvalidaCom400() throws Exception {
    UUID userId = UUID.randomUUID();

    mvc.perform(
            post(BASE)
                .with(asUser(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"X\",\"color\":\"vermelho\"}"))
        .andExpect(status().isBadRequest());
  }
}
