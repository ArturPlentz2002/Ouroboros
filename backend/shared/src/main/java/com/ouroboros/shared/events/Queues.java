package com.ouroboros.shared.events;

/** Nomes das filas RabbitMQ (tarefas/comandos). Contrato compartilhado entre os servicos. */
public final class Queues {

  private Queues() {}

  public static final String NOTIFICATIONS_EMAIL = "notifications.email";
  public static final String NOTIFICATIONS_PUSH = "notifications.push";
  public static final String REPORTS_GENERATE = "reports.generate";
}
