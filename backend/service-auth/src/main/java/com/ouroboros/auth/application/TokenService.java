package com.ouroboros.auth.application;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/** Emite access tokens JWT assinados em RS256. */
@Service
public class TokenService {

  private final JwtEncoder jwtEncoder;
  private final String issuer;
  private final String audience;
  private final Duration accessTokenTtl;

  public TokenService(
      JwtEncoder jwtEncoder,
      @Value("${ouroboros.jwt.issuer:ouroboros-auth}") String issuer,
      @Value("${ouroboros.jwt.audience:ouroboros}") String audience,
      @Value("${ouroboros.jwt.access-token-ttl:PT15M}") Duration accessTokenTtl) {
    this.jwtEncoder = jwtEncoder;
    this.issuer = issuer;
    this.audience = audience;
    this.accessTokenTtl = accessTokenTtl;
  }

  /** Gera um access token com subject = id do usuario, audience da plataforma e claim email. */
  public String issueAccessToken(UUID userId, String email) {
    Instant now = Instant.now();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(issuer)
            .audience(List.of(audience))
            .issuedAt(now)
            .expiresAt(now.plus(accessTokenTtl))
            .subject(userId.toString())
            .claim("email", email)
            .build();
    JwsHeader header = JwsHeader.with(() -> "RS256").build();
    return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  }

  public long accessTokenTtlSeconds() {
    return accessTokenTtl.toSeconds();
  }
}
