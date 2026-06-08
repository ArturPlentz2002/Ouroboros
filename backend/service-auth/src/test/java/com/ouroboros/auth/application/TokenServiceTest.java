package com.ouroboros.auth.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

class TokenServiceTest {

  private TokenService tokenService;
  private JwtDecoder decoder;

  @BeforeEach
  void setUp() throws Exception {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);
    KeyPair keyPair = generator.generateKeyPair();
    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
    RSAKey rsaKey =
        new RSAKey.Builder(publicKey).privateKey(keyPair.getPrivate()).keyID("test").build();
    JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));
    JwtEncoder encoder = new NimbusJwtEncoder(jwkSource);
    this.tokenService =
        new TokenService(encoder, "ouroboros-auth", "ouroboros", Duration.ofMinutes(15));
    this.decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
  }

  @Test
  void emiteTokenRs256ComSubjectEmailEIssuer() {
    UUID userId = UUID.randomUUID();

    String token = tokenService.issueAccessToken(userId, "ana@ouroboros.dev");

    Jwt jwt = decoder.decode(token);
    assertThat(jwt.getSubject()).isEqualTo(userId.toString());
    assertThat(jwt.getClaimAsString("email")).isEqualTo("ana@ouroboros.dev");
    assertThat(jwt.getClaimAsString("iss")).isEqualTo("ouroboros-auth");
    assertThat(jwt.getAudience()).containsExactly("ouroboros");
    assertThat(jwt.getHeaders().get("alg")).hasToString("RS256");
    assertThat(jwt.getExpiresAt()).isNotNull();
  }
}
