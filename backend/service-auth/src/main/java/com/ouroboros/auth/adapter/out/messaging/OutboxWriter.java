package com.ouroboros.auth.adapter.out.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Grava eventos na tabela de outbox. Deve ser chamado DENTRO da transacao da operacao de dominio
 * para que evento e dados sejam atomicos.
 */
@Component
public class OutboxWriter {

  private final OutboxEventRepository outbox;
  private final ObjectMapper mapper;

  public OutboxWriter(OutboxEventRepository outbox, ObjectMapper mapper) {
    this.outbox = outbox;
    this.mapper = mapper;
  }

  /**
   * Serializa o payload e enfileira o evento no outbox.
   *
   * @param eventId id do evento (= id da linha do outbox; chave de idempotencia)
   * @param topic topico Kafka de destino
   * @param aggregateType nome do agregado de origem (ex.: "User")
   * @param aggregateId id do agregado (vira a chave da mensagem Kafka)
   * @param payload objeto do evento, serializado em JSON
   */
  public void write(
      UUID eventId, String topic, String aggregateType, UUID aggregateId, Object payload) {
    try {
      String json = mapper.writeValueAsString(payload);
      outbox.save(OutboxEvent.create(eventId, aggregateType, aggregateId, topic, json));
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Falha ao serializar evento para o outbox: " + topic, e);
    }
  }
}
