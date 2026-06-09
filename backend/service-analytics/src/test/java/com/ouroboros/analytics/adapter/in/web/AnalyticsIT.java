package com.ouroboros.analytics.adapter.in.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ouroboros.analytics.application.AnalyticsService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AnalyticsIT {

  private static final String BASE = "/api/v1/analytics";

  @Container
  static CassandraContainer<?> cassandra =
      new CassandraContainer<>(DockerImageName.parse("cassandra:5"))
          .withInitScript("cassandra-init.cql");

  @DynamicPropertySource
  static void cassandraProps(DynamicPropertyRegistry registry) {
    registry.add("spring.cassandra.contact-points", cassandra::getHost);
    registry.add("spring.cassandra.port", () -> cassandra.getMappedPort(9042));
    registry.add("spring.cassandra.local-datacenter", cassandra::getLocalDatacenter);
    registry.add("spring.cassandra.keyspace-name", () -> "ouroboros");
  }

  @Autowired private MockMvc mvc;
  @Autowired private AnalyticsService service;

  private static RequestPostProcessor asUser(UUID userId) {
    return jwt().jwt(builder -> builder.subject(userId.toString()));
  }

  @Test
  void semTokenRetorna401() throws Exception {
    mvc.perform(get(BASE + "/activity")).andExpect(status().isUnauthorized());
  }

  @Test
  void registraEConsultaAtividadeOrdenadaESumario() throws Exception {
    UUID user = UUID.randomUUID();
    service.record(
        user,
        "e1",
        "agenda.event.created",
        "service-agenda",
        Instant.parse("2026-01-01T10:00:00Z"));
    service.record(
        user,
        "e2",
        "finance.entry.created",
        "service-finance",
        Instant.parse("2026-01-02T10:00:00Z"));

    mvc.perform(get(BASE + "/activity").with(asUser(user)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].eventId").value("e2"))
        .andExpect(jsonPath("$[1].eventId").value("e1"));

    mvc.perform(get(BASE + "/summary").with(asUser(user)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$['agenda.event.created']").value(1))
        .andExpect(jsonPath("$['finance.entry.created']").value(1));
  }

  @Test
  void atividadeEscopadaPorUsuario() throws Exception {
    UUID owner = UUID.randomUUID();
    UUID other = UUID.randomUUID();
    service.record(
        owner,
        "e1",
        "agenda.event.created",
        "service-agenda",
        Instant.parse("2026-01-01T10:00:00Z"));

    mvc.perform(get(BASE + "/activity").with(asUser(other)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }
}
