package com.ouroboros.finance.adapter.in.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class SummaryIT {

  private static final String ENTRIES = "/api/v1/finance/entries";
  private static final String SUMMARY = "/api/v1/finance/summary";

  @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @DynamicPropertySource
  static void datasource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("ouroboros.outbox.relay.enabled", () -> "false");
  }

  @Autowired private MockMvc mvc;

  private static RequestPostProcessor asUser(UUID userId) {
    return jwt().jwt(builder -> builder.subject(userId.toString()));
  }

  private void createEntry(UUID userId, String type, String amount, String occurredOn)
      throws Exception {
    String body =
        "{\"type\":\""
            + type
            + "\",\"amount\":"
            + amount
            + ",\"occurredOn\":\""
            + occurredOn
            + "\"}";
    mvc.perform(
            post(ENTRIES)
                .with(asUser(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isCreated());
  }

  @Test
  void semTokenRetorna401() throws Exception {
    mvc.perform(get(SUMMARY).param("month", "2026-06")).andExpect(status().isUnauthorized());
  }

  @Test
  void resumeApenasOMesPedido() throws Exception {
    UUID userId = UUID.randomUUID();
    createEntry(userId, "INCOME", "1000.00", "2026-06-05");
    createEntry(userId, "EXPENSE", "200.00", "2026-06-10");
    createEntry(userId, "EXPENSE", "50.50", "2026-06-20");
    // Fora do mes pedido: deve ser ignorado.
    createEntry(userId, "EXPENSE", "999.00", "2026-07-01");

    mvc.perform(get(SUMMARY).param("month", "2026-06").with(asUser(userId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.month").value("2026-06"))
        .andExpect(jsonPath("$.totalIncome").value(1000.00))
        .andExpect(jsonPath("$.totalExpense").value(250.50))
        .andExpect(jsonPath("$.balance").value(749.50))
        .andExpect(jsonPath("$.byCategory.length()").value(2));
  }

  @Test
  void mesSemLancamentosRetornaZeros() throws Exception {
    UUID userId = UUID.randomUUID();

    mvc.perform(get(SUMMARY).param("month", "2026-01").with(asUser(userId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalIncome").value(0.00))
        .andExpect(jsonPath("$.totalExpense").value(0.00))
        .andExpect(jsonPath("$.balance").value(0.00))
        .andExpect(jsonPath("$.byCategory.length()").value(0));
  }

  @Test
  void mesEmFormatoInvalidoRetorna400() throws Exception {
    UUID userId = UUID.randomUUID();

    mvc.perform(get(SUMMARY).param("month", "junho").with(asUser(userId)))
        .andExpect(status().isBadRequest());
  }
}
