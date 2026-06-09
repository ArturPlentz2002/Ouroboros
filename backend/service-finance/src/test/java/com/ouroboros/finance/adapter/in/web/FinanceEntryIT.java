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
class FinanceEntryIT {

  private static final String BASE = "/api/v1/finance/entries";
  private static final String CATEGORIES = "/api/v1/finance/categories";

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

  private static String entryJson(String type, String amount, String occurredOn) {
    return "{\"type\":\""
        + type
        + "\",\"amount\":"
        + amount
        + ",\"description\":\"teste\",\"occurredOn\":\""
        + occurredOn
        + "\"}";
  }

  private String createEntry(UUID userId, String type, String amount) throws Exception {
    String body =
        mvc.perform(
                post(BASE)
                    .with(asUser(userId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(entryJson(type, amount, "2026-06-01")))
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

    createEntry(userA, "EXPENSE", "42.50");

    mvc.perform(get(BASE).with(asUser(userA)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].type").value("EXPENSE"))
        .andExpect(jsonPath("$[0].amount").value(42.50))
        .andExpect(jsonPath("$[0].categoryId").doesNotExist());

    mvc.perform(get(BASE).with(asUser(userB)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void lancamentoDeOutroUsuarioRetorna404() throws Exception {
    UUID owner = UUID.randomUUID();
    UUID other = UUID.randomUUID();
    String id = createEntry(owner, "INCOME", "1000");

    mvc.perform(get(BASE + "/" + id).with(asUser(other))).andExpect(status().isNotFound());
    mvc.perform(get(BASE + "/" + id).with(asUser(owner))).andExpect(status().isOk());
  }

  @Test
  void atualizaEDeleta() throws Exception {
    UUID userId = UUID.randomUUID();
    String id = createEntry(userId, "EXPENSE", "10");

    mvc.perform(
            put(BASE + "/" + id)
                .with(asUser(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(entryJson("INCOME", "250.75", "2026-06-15")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.type").value("INCOME"))
        .andExpect(jsonPath("$.amount").value(250.75))
        .andExpect(jsonPath("$.occurredOn").value("2026-06-15"));

    mvc.perform(delete(BASE + "/" + id).with(asUser(userId))).andExpect(status().isNoContent());
    mvc.perform(get(BASE + "/" + id).with(asUser(userId))).andExpect(status().isNotFound());
  }

  @Test
  void rejeitaValorNaoPositivoCom400() throws Exception {
    UUID userId = UUID.randomUUID();

    mvc.perform(
            post(BASE)
                .with(asUser(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(entryJson("EXPENSE", "0", "2026-06-01")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void rejeitaCategoriaInexistenteCom404() throws Exception {
    UUID userId = UUID.randomUUID();
    String body =
        "{\"type\":\"EXPENSE\",\"amount\":10,\"occurredOn\":\"2026-06-01\",\"categoryId\":\""
            + UUID.randomUUID()
            + "\"}";

    mvc.perform(
            post(BASE).with(asUser(userId)).contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isNotFound());
  }

  @Test
  void vinculaLancamentoAUmaCategoriaPropria() throws Exception {
    UUID userId = UUID.randomUUID();
    String categoryBody =
        mvc.perform(
                post(CATEGORIES)
                    .with(asUser(userId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Mercado\"}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String categoryId = objectMapper.readTree(categoryBody).get("id").asText();

    String entryBody =
        "{\"type\":\"EXPENSE\",\"amount\":33.30,\"occurredOn\":\"2026-06-01\",\"categoryId\":\""
            + categoryId
            + "\"}";

    mvc.perform(
            post(BASE)
                .with(asUser(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(entryBody))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.categoryId").value(categoryId));
  }
}
