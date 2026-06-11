import React, { useState } from 'react';
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import { colors, spacing, typography } from '../../design/tokens';
import { useAgendaEvents, useCreateAgendaEvent, useRemoveAgendaEvent } from '../../hooks/useAgenda';
import { Button } from '../../ui/Button';
import { Card } from '../../ui/Card';
import { EmptyState } from '../../ui/EmptyState';
import { Input } from '../../ui/Input';
import { AgendaEvent } from './agendaApi';

/** Lista e criacao de eventos da agenda. */
export function AgendaScreen(): React.ReactElement {
  const events = useAgendaEvents();
  const create = useCreateAgendaEvent();
  const remove = useRemoveAgendaEvent();

  const [title, setTitle] = useState('');
  const [startsAt, setStartsAt] = useState('');
  const [formError, setFormError] = useState<string | undefined>();

  const submit = () => {
    const iso = parseDateTime(startsAt);
    if (!title.trim()) {
      setFormError('Informe um título');
      return;
    }
    if (!iso) {
      setFormError('Data inválida — use AAAA-MM-DD HH:mm');
      return;
    }
    setFormError(undefined);
    create.mutate(
      { title: title.trim(), startsAt: iso },
      {
        onSuccess: () => {
          setTitle('');
          setStartsAt('');
        },
      },
    );
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Text style={styles.heading}>Agenda</Text>

      <Card>
        <Input label="Título" value={title} onChangeText={setTitle} testID="agenda-title" />
        <Input
          label="Início"
          value={startsAt}
          onChangeText={setStartsAt}
          placeholder="2026-06-15 14:30"
          error={formError}
          testID="agenda-starts-at"
        />
        <Button
          title="Criar evento"
          onPress={submit}
          loading={create.isPending}
          testID="agenda-create"
        />
        {create.isError ? (
          <Text style={styles.error}>Não foi possível criar o evento. Tente novamente.</Text>
        ) : null}
      </Card>

      {events.isLoading ? <Text style={styles.muted}>Carregando…</Text> : null}
      {events.isError ? <Text style={styles.error}>Não foi possível carregar a agenda.</Text> : null}
      {events.data?.length === 0 ? (
        <EmptyState title="Sem eventos" hint="Crie o primeiro evento acima" />
      ) : null}

      {events.data?.map((event: AgendaEvent) => (
        <Card key={event.id} testID={`agenda-event-${event.id}`}>
          <Text style={styles.itemTitle}>{event.title}</Text>
          <Text style={styles.muted}>{formatDateTime(event.startsAt)}</Text>
          {event.description ? <Text style={styles.body}>{event.description}</Text> : null}
          <View style={styles.actions}>
            <Button
              title="Excluir"
              variant="danger"
              onPress={() => remove.mutate(event.id)}
              testID={`agenda-remove-${event.id}`}
            />
          </View>
        </Card>
      ))}
    </ScrollView>
  );
}

/** Converte "AAAA-MM-DD HH:mm" (hora local) para ISO-8601 UTC; null se invalido. */
export function parseDateTime(text: string): string | null {
  const normalized = text.trim().replace(' ', 'T');
  if (!/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/.test(normalized)) {
    return null;
  }
  const date = new Date(normalized);
  return Number.isNaN(date.getTime()) ? null : date.toISOString();
}

function formatDateTime(iso: string): string {
  const date = new Date(iso);
  return Number.isNaN(date.getTime()) ? iso : date.toLocaleString();
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  content: {
    padding: spacing.md,
  },
  heading: {
    fontSize: typography.fontSizes.title,
    fontWeight: typography.weights.bold,
    color: colors.text,
    marginBottom: spacing.md,
  },
  itemTitle: {
    fontSize: typography.fontSizes.subtitle,
    fontWeight: typography.weights.medium,
    color: colors.text,
  },
  body: {
    marginTop: spacing.xs,
    color: colors.text,
  },
  muted: {
    color: colors.textMuted,
    fontSize: typography.fontSizes.body,
  },
  error: {
    color: colors.danger,
  },
  actions: {
    marginTop: spacing.sm,
    alignSelf: 'flex-start',
  },
});
