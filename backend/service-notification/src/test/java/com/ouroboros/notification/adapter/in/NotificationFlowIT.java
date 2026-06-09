package com.ouroboros.notification.adapter.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ouroboros.notification.adapter.out.persistence.NotificationRepository;
import com.ouroboros.notification.domain.Notification;
import com.ouroboros.shared.events.Topics;
import com.ouroboros.shared.events.UserRegisteredEvent;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class NotificationFlowIT {

  @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  @Container
  static KafkaContainer kafka =
      new KafkaContainer("apache/kafka:3.8.1").withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true");

  @Container static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management");

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    registry.add("spring.rabbitmq.host", rabbit::getHost);
    registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
    registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
    registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
  }

  @Autowired private NotificationRepository notifications;
  @Autowired private ObjectMapper mapper;
  @Autowired private MockMvc mvc;

  @Test
  void eventoUserRegisteredViraNotificacaoEnviadaEEhIdempotente() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID eventId = UUID.randomUUID();
    UserRegisteredEvent event =
        new UserRegisteredEvent(eventId.toString(), userId, "novo@ouroboros.dev", Instant.now());
    String payload = mapper.writeValueAsString(event);

    publish(Topics.USER_REGISTERED, userId.toString(), payload);

    // O consumidor Kafka cria a notificacao.
    Notification notification = awaitNotification(userId);
    assertThat(notification).as("notificacao deve ser criada pelo consumidor").isNotNull();
    assertThat(notification.getType()).isEqualTo("USER_REGISTERED");
    assertThat(notification.getTitle()).isEqualTo("Bem-vindo ao Ouroboros!");

    // A tarefa RabbitMQ marca como enviada (round-trip pela fila notifications.email).
    awaitEmailed(notification.getId());

    // Idempotencia: republicar o MESMO evento nao cria outra notificacao.
    publish(Topics.USER_REGISTERED, userId.toString(), payload);
    Thread.sleep(3000);
    assertThat(notifications.findByUserIdOrderByCreatedAtDesc(userId)).hasSize(1);

    // A API expoe a notificacao do usuario (escopada pelo subject do JWT).
    mvc.perform(get("/api/v1/notifications").with(asUser(userId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].type").value("USER_REGISTERED"))
        .andExpect(jsonPath("$[0].emailedAt").isNotEmpty());
  }

  private void publish(String topic, String key, String value) throws Exception {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
      producer.send(new ProducerRecord<>(topic, key, value)).get();
    }
  }

  private Notification awaitNotification(UUID userId) throws InterruptedException {
    long deadline = System.currentTimeMillis() + 60_000;
    while (System.currentTimeMillis() < deadline) {
      List<Notification> found = notifications.findByUserIdOrderByCreatedAtDesc(userId);
      if (!found.isEmpty()) {
        return found.get(0);
      }
      Thread.sleep(500);
    }
    return null;
  }

  private void awaitEmailed(UUID notificationId) throws InterruptedException {
    long deadline = System.currentTimeMillis() + 90_000;
    while (System.currentTimeMillis() < deadline) {
      Notification n = notifications.findById(notificationId).orElseThrow();
      if (n.getEmailedAt() != null) {
        return;
      }
      Thread.sleep(500);
    }
    throw new AssertionError("notificacao nao foi marcada como enviada (RabbitMQ)");
  }

  private static org.springframework.test.web.servlet.request.RequestPostProcessor asUser(
      UUID userId) {
    return jwt().jwt(builder -> builder.subject(userId.toString()));
  }
}
