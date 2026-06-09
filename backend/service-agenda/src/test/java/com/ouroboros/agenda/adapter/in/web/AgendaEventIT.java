package com.ouroboros.agenda.adapter.in.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
class AgendaEventIT {

  private static final String BASE = "/api/v1/agenda/events";

  @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @DynamicPropertySource
  static void datasource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("ouroboros.outbox.relay.enabled", () -> "false");
  }

  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper objectMapper;

  private static RequestPostProcessor asUser(UUID userId) {
    return jwt().jwt(builder -> builder.subject(userId.toString()));
  }

  private static String eventJson(String title) {
    return "{\"title\":\""
        + title
        + "\",\"description\":\"d\",\"startsAt\":\"2026-07-01T10:00:00Z\","
        + "\"endsAt\":\"2026-07-01T11:00:00Z\"}";
  }

  private String createEvent(UUID userId, String title) throws Exception {
    String body =
        mvc.perform(
                post(BASE)
                    .with(asUser(userId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(eventJson(title)))
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
  void metricasPrometheusExpostasSemAuth() throws Exception {
    mvc.perform(get("/actuator/prometheus"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("jvm_")));
  }

  @Test
  void criaEListaEscopadoPorUsuario() throws Exception {
    UUID userA = UUID.randomUUID();
    UUID userB = UUID.randomUUID();

    createEvent(userA, "Reuniao A");

    mvc.perform(get(BASE).with(asUser(userA)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].title").value("Reuniao A"));

    mvc.perform(get(BASE).with(asUser(userB)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void eventoDeOutroUsuarioRetorna404() throws Exception {
    UUID owner = UUID.randomUUID();
    UUID other = UUID.randomUUID();
    String id = createEvent(owner, "Privado");

    mvc.perform(get(BASE + "/" + id).with(asUser(other))).andExpect(status().isNotFound());
    mvc.perform(get(BASE + "/" + id).with(asUser(owner))).andExpect(status().isOk());
  }

  @Test
  void atualizaEDeleta() throws Exception {
    UUID userId = UUID.randomUUID();
    String id = createEvent(userId, "Original");

    mvc.perform(
            put(BASE + "/" + id)
                .with(asUser(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(eventJson("Atualizado")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Atualizado"));

    mvc.perform(delete(BASE + "/" + id).with(asUser(userId))).andExpect(status().isNoContent());
    mvc.perform(get(BASE + "/" + id).with(asUser(userId))).andExpect(status().isNotFound());
  }

  @Test
  void rejeitaIntervaloInvalidoCom400() throws Exception {
    UUID userId = UUID.randomUUID();
    String invalid =
        "{\"title\":\"X\",\"startsAt\":\"2026-07-01T11:00:00Z\","
            + "\"endsAt\":\"2026-07-01T10:00:00Z\"}";

    mvc.perform(
            post(BASE)
                .with(asUser(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalid))
        .andExpect(status().isBadRequest());
  }
}
