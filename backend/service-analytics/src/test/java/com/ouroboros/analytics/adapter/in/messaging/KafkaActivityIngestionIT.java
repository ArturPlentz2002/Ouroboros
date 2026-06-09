package com.ouroboros.analytics.adapter.in.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ouroboros.analytics.adapter.out.persistence.UserActivityRepository;
import com.ouroboros.analytics.domain.UserActivity;
import com.ouroboros.shared.events.AgendaEventCreatedEvent;
import com.ouroboros.shared.events.Topics;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
class KafkaActivityIngestionIT {

  @Container
  static CassandraContainer<?> cassandra =
      new CassandraContainer<>(DockerImageName.parse("cassandra:5"))
          .withInitScript("cassandra-init.cql");

  @Container
  static KafkaContainer kafka =
      new KafkaContainer("apache/kafka:3.8.1").withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true");

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry registry) {
    registry.add("spring.cassandra.contact-points", cassandra::getHost);
    registry.add("spring.cassandra.port", () -> cassandra.getMappedPort(9042));
    registry.add("spring.cassandra.local-datacenter", cassandra::getLocalDatacenter);
    registry.add("spring.cassandra.keyspace-name", () -> "ouroboros");
    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
  }

  @Autowired private UserActivityRepository activities;
  @Autowired private ObjectMapper mapper;

  private KafkaProducer<String, String> newProducer() {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    return new KafkaProducer<>(props);
  }

  @Test
  void consomeEventoDaAgendaERegistraAtividade() throws Exception {
    UUID user = UUID.randomUUID();
    Instant occurred = Instant.parse("2026-03-01T09:00:00Z");
    var event =
        new AgendaEventCreatedEvent(
            UUID.randomUUID().toString(), user, UUID.randomUUID(), "Reuniao", occurred, occurred);

    try (KafkaProducer<String, String> producer = newProducer()) {
      producer
          .send(
              new ProducerRecord<>(
                  Topics.AGENDA_EVENT_CREATED, user.toString(), mapper.writeValueAsString(event)))
          .get();
    }

    await()
        .atMost(Duration.ofSeconds(60))
        .untilAsserted(() -> assertThat(activities.findByKeyUserId(user)).hasSize(1));

    List<UserActivity> result = activities.findByKeyUserId(user);
    assertThat(result.get(0).getEventType()).isEqualTo(Topics.AGENDA_EVENT_CREATED);
    assertThat(result.get(0).getSource()).isEqualTo("service-agenda");
  }
}
