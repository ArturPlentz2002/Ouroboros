package com.ouroboros.notification.application;

import java.util.UUID;

/**
 * Tarefa de envio de e-mail publicada na fila {@code notifications.email} (RabbitMQ). Comando, nao
 * evento: instrui o worker a "enviar" o e-mail da notificacao.
 */
public record EmailTask(UUID notificationId, UUID userId, String subject) {}
