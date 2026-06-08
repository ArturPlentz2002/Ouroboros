package com.ouroboros.agenda.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class EventTimeValidatorTest {

  private static final Instant START = Instant.parse("2026-07-01T10:00:00Z");

  @Test
  void aceitaFimAposInicio() {
    assertThatCode(() -> EventTimeValidator.validate(START, START.plus(Duration.ofHours(1))))
        .doesNotThrowAnyException();
  }

  @Test
  void aceitaSemFim() {
    assertThatCode(() -> EventTimeValidator.validate(START, null)).doesNotThrowAnyException();
  }

  @Test
  void rejeitaFimAntesDoInicio() {
    assertThatThrownBy(() -> EventTimeValidator.validate(START, START.minus(Duration.ofHours(1))))
        .isInstanceOf(InvalidEventTimeException.class);
  }

  @Test
  void rejeitaFimIgualAoInicio() {
    assertThatThrownBy(() -> EventTimeValidator.validate(START, START))
        .isInstanceOf(InvalidEventTimeException.class);
  }

  @Test
  void rejeitaInicioNulo() {
    assertThatThrownBy(() -> EventTimeValidator.validate(null, START))
        .isInstanceOf(InvalidEventTimeException.class);
  }
}
