package com.ouroboros.auth.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ouroboros.auth.application.social.InvalidSocialTokenException;
import com.ouroboros.auth.application.social.SocialIdTokenVerifier;
import com.ouroboros.auth.application.social.SocialIdentity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Login social com um verificador fake (o verificador real do Google fica desativado). */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = "ouroboros.social.google.enabled=false")
class SocialLoginIT {

  @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @DynamicPropertySource
  static void datasource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("ouroboros.outbox.relay.enabled", () -> "false");
  }

  @TestConfiguration
  static class FakeVerifierConfig {
    @Bean
    SocialIdTokenVerifier fakeGoogleVerifier() {
      return new SocialIdTokenVerifier() {
        @Override
        public String provider() {
          return "GOOGLE";
        }

        @Override
        public SocialIdentity verify(String idToken) {
          if ("valid".equals(idToken)) {
            return new SocialIdentity("GOOGLE", "g-sub-123", "social@ouroboros.dev");
          }
          throw new InvalidSocialTokenException("fake invalido");
        }
      };
    }
  }

  @Autowired private MockMvc mvc;

  private void social(String provider, String idToken, int expectedStatus) throws Exception {
    mvc.perform(
            post("/auth/social")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"provider\":\"" + provider + "\",\"idToken\":\"" + idToken + "\"}"))
        .andExpect(status().is(expectedStatus));
  }

  @Test
  void loginSocialValidoEmiteTokensEReusaUsuarioPorEmail() throws Exception {
    mvc.perform(
            post("/auth/social")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"provider\":\"google\",\"idToken\":\"valid\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists())
        .andExpect(jsonPath("$.refreshToken").exists())
        .andExpect(jsonPath("$.tokenType").value("Bearer"));

    // segunda chamada com o mesmo e-mail nao duplica e segue emitindo tokens
    social("google", "valid", 200);

    // conta social nao tem senha: login por e-mail/senha falha com 401
    mvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"social@ouroboros.dev\",\"password\":\"qualquer1\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void idTokenInvalidoRetorna401() throws Exception {
    social("google", "bad", 401);
  }

  @Test
  void provedorNaoSuportadoRetorna400() throws Exception {
    social("facebook", "valid", 400);
  }
}
