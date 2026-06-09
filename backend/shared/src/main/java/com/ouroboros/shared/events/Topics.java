package com.ouroboros.shared.events;

/** Nomes dos topicos Kafka (eventos de dominio). Contrato compartilhado entre os servicos. */
public final class Topics {

  private Topics() {}

  public static final String USER_REGISTERED = "user.registered";
  public static final String AGENDA_EVENT_CREATED = "agenda.event.created";
  public static final String AGENDA_EVENT_REMINDER_DUE = "agenda.event.reminder.due";
  public static final String FINANCE_ENTRY_CREATED = "finance.entry.created";
  public static final String FINANCE_ENTRY_UPDATED = "finance.entry.updated";
  public static final String FINANCE_ENTRY_DELETED = "finance.entry.deleted";
}
