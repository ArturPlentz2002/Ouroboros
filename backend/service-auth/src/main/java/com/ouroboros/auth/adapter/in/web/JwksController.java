package com.ouroboros.auth.adapter.in.web;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Expoe a chave publica (JWKS) usada para validar os JWT emitidos. */
@RestController
public class JwksController {

  private final RSAKey rsaKey;

  public JwksController(RSAKey rsaKey) {
    this.rsaKey = rsaKey;
  }

  @GetMapping("/oauth2/jwks")
  public Map<String, Object> jwks() {
    // Apenas a parte publica da chave.
    return new JWKSet(rsaKey.toPublicJWK()).toJSONObject();
  }
}
