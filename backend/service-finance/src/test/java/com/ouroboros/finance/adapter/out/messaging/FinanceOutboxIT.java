package com.ouroboros.finance.adapter.out.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ouroboros.finance.application.FinanceEntryService;
import com.ouroboros.finance.domain.EntryType;
import com.ouroboros.finance.domain.FinanceEntry;
import com.ouroboros.shared.events.Topics;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

@SpringBootTest
@Testcontainers
class FinanceOutboxIT {

  @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @Container
  static KafkaContainer kafka =
      new KafkaContainer("apache/kafka:3.8.1").withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true");

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    // Desliga o scheduler do relay no teste; publicamos explicitamente para ser deterministico.
    registry.add("ouroboros.outbox.poll-interval-ms", () -> "3600000");
  }

  @Autowired private FinanceEntryService service;
  @Autowired private OutboxRelay relay;
  @Autowired private OutboxEventRepository outbox;
  @Autowired private ObjectMapper mapper;

  @Test
  void criaLancamentoPublicaEventoNoKafka() throws Exception {
    UUID userId = UUID.randomUUID();

    FinanceEntry entry =
        service.create(
            userId,
            null,
            EntryType.EXPENSE,
            new BigDecimal("42.50"),
            "Feira",
            LocalDate.of(2026, 6, 1),
            null);

    // O evento foi gravado no outbox na mesma transacao (ainda pendente).
    assertThat(outbox.findTop100BySentAtIsNullOrderByCreatedAtAsc()).hasSize(1);

    relay.publishPending();

    // Apos publicar, nao ha mais pendentes.
    assertThat(outbox.findTop100BySentAtIsNullOrderByCreatedAtAsc()).isEmpty();

    try (KafkaConsumer<String, String> consumer = newConsumer()) {
      consumer.subscribe(List.of(Topics.FINANCE_ENTRY_CREATED));
      ConsumerRecord<String, String> record = pollOne(consumer);

      assertThat(record).as("deve receber o evento finance.entry.created").isNotNull();
      assertThat(record.key()).isEqualTo(entry.getId().toString());

      JsonNode json = mapper.readTree(record.value());
      assertThat(json.get("entryId").asText()).isEqualTo(entry.getId().toString());
      assertThat(json.get("userId").asText()).isEqualTo(userId.toString());
      assertThat(json.get("changeType").asText()).isEqualTo("CREATED");
      assertThat(json.get("type").asText()).isEqualTo("EXPENSE");
      assertThat(new BigDecimal(json.get("amount").asText())).isEqualByComparingTo("42.50");
    }
  }

  private KafkaConsumer<String, String> newConsumer() {
    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "it-" + UUID.randomUUID());
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    return new KafkaConsumer<>(props);
  }

  private static ConsumerRecord<String, String> pollOne(KafkaConsumer<String, String> consumer) {
    long deadline = System.currentTimeMillis() + 20_000;
    while (System.currentTimeMillis() < deadline) {
      ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
      if (!records.isEmpty()) {
        return records.iterator().next();
      }
    }
    return null;
  }
}
