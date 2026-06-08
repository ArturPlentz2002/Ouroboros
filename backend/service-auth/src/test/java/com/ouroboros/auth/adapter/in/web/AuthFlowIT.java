package com.ouroboros.auth.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthFlowIT {

  @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @DynamicPropertySource
  static void datasource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper objectMapper;

  private void register(String email, String password) throws Exception {
    mvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
        .andExpect(status().isCreated());
  }

  private String login(String email, String password) throws Exception {
    return mvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists())
        .andExpect(jsonPath("$.refreshToken").exists())
        .andExpect(jsonPath("$.tokenType").value("Bearer"))
        .andReturn()
        .getResponse()
        .getContentAsString();
  }

  @Test
  void fluxoRegistroLoginRefreshComRotacao() throws Exception {
    register("flow@ouroboros.dev", "password1");

    String loginBody = login("flow@ouroboros.dev", "password1");
    String oldRefresh = objectMapper.readTree(loginBody).get("refreshToken").asText();

    String refreshBody =
        mvc.perform(
                post("/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"refreshToken\":\"" + oldRefresh + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String newRefresh = objectMapper.readTree(refreshBody).get("refreshToken").asText();

    assertThat(newRefresh).isNotEqualTo(oldRefresh);

    // o refresh antigo foi rotacionado -> nao pode mais ser usado
    mvc.perform(
            post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + oldRefresh + "\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void logoutRevogaRefreshToken() throws Exception {
    register("logout@ouroboros.dev", "password1");
    String loginBody = login("logout@ouroboros.dev", "password1");
    String refresh = objectMapper.readTree(loginBody).get("refreshToken").asText();

    mvc.perform(
            post("/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refresh + "\"}"))
        .andExpect(status().isNoContent());

    // o refresh token revogado nao pode mais renovar
    mvc.perform(
            post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refresh + "\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void loginComSenhaErradaRetorna401() throws Exception {
    register("wrong@ouroboros.dev", "password1");

    mvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"wrong@ouroboros.dev\",\"password\":\"errada123\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void jwksExpoeChavePublicaRsa() throws Exception {
    mvc.perform(get("/oauth2/jwks"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.keys").isArray())
        .andExpect(jsonPath("$.keys[0].kty").value("RSA"));
  }
}
