package com.ouroboros.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Verifica a seguranca de borda do gateway sem depender dos servicos downstream: o contexto sobe e
 * rotas protegidas exigem autenticacao (401 antes de rotear).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewaySecurityTest {

  @Autowired private WebTestClient client;

  @Test
  void contextLoads() {
    // valida que o gateway (rotas + resource server reativo) inicializa
  }

  @Test
  void rotaProtegidaSemTokenRetorna401() {
    client.get().uri("/api/v1/agenda/events").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void preflightCorsEhPermitido() {
    client
        .options()
        .uri("/api/v1/agenda/events")
        .header("Origin", "http://localhost:3000")
        .header("Access-Control-Request-Method", "GET")
        .exchange()
        .expectStatus()
        .is2xxSuccessful();
  }
}
