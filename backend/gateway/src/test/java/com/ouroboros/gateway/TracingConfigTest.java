package com.ouroboros.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Garante que o tracing (Micrometer + Brave) esta ativo: ha um {@link Tracer} no contexto. */
@SpringBootTest
class TracingConfigTest {

  @Autowired(required = false)
  private Tracer tracer;

  @Test
  void tracerDisponivelNoContexto() {
    assertThat(tracer).isNotNull();
  }
}
