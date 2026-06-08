package com.ouroboros.auth.adapter.out.social;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.ouroboros.auth.application.social.InvalidSocialTokenException;
import com.ouroboros.auth.application.social.SocialIdTokenVerifier;
import com.ouroboros.auth.application.social.SocialIdentity;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Valida ID tokens do Google: verifica assinatura RS256 contra o JWKS do Google e os claims iss,
 * aud (client-id) e exp. Desativavel via {@code ouroboros.social.google.enabled=false} (usado em
 * testes com um verificador fake).
 */
@Component
@ConditionalOnProperty(
    prefix = "ouroboros.social.google",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public class GoogleIdTokenVerifier implements SocialIdTokenVerifier {

  private static final Set<String> ALLOWED_ISSUERS =
      Set.of("https://accounts.google.com", "accounts.google.com");

  private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;
  private final String clientId;

  public GoogleIdTokenVerifier(
      @Value("${ouroboros.social.google.client-id:}") String clientId,
      @Value("${ouroboros.social.google.jwks-uri:https://www.googleapis.com/oauth2/v3/certs}")
          String jwksUri) {
    this.clientId = clientId;
    try {
      JWKSource<SecurityContext> jwkSource =
          JWKSourceBuilder.create(URI.create(jwksUri).toURL()).build();
      DefaultJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
      processor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource));
      this.jwtProcessor = processor;
    } catch (Exception e) {
      throw new IllegalStateException("Falha ao configurar o verificador do Google", e);
    }
  }

  @Override
  public String provider() {
    return "GOOGLE";
  }

  @Override
  public SocialIdentity verify(String idToken) {
    try {
      JWTClaimsSet claims = jwtProcessor.process(idToken, null);
      if (!ALLOWED_ISSUERS.contains(claims.getIssuer())) {
        throw new InvalidSocialTokenException("Issuer invalido");
      }
      List<String> audience = claims.getAudience();
      if (!clientId.isBlank() && (audience == null || !audience.contains(clientId))) {
        throw new InvalidSocialTokenException("Audience invalido");
      }
      Date expiration = claims.getExpirationTime();
      if (expiration == null || expiration.before(new Date())) {
        throw new InvalidSocialTokenException("Token expirado");
      }
      String subject = claims.getSubject();
      String email = claims.getStringClaim("email");
      if (subject == null || email == null) {
        throw new InvalidSocialTokenException("Claims obrigatorios ausentes (sub/email)");
      }
      return new SocialIdentity(provider(), subject, email);
    } catch (InvalidSocialTokenException e) {
      throw e;
    } catch (Exception e) {
      throw new InvalidSocialTokenException("ID token do Google invalido", e);
    }
  }
}
