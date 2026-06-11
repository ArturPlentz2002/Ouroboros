import React, { useState } from 'react';
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import { colors, spacing, typography } from '../../design/tokens';
import { useCreateNote, useNotes, useRemoveNote } from '../../hooks/useNotes';
import { Button } from '../../ui/Button';
import { Card } from '../../ui/Card';
import { Chip } from '../../ui/Chip';
import { EmptyState } from '../../ui/EmptyState';
import { Input } from '../../ui/Input';
import { Note } from './notesApi';

/** Notas com busca por texto e criacao com tags. */
export function NotesScreen(): React.ReactElement {
  const [search, setSearch] = useState('');
  const notes = useNotes(search.trim() ? { q: search.trim() } : {});
  const create = useCreateNote();
  const remove = useRemoveNote();

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [tags, setTags] = useState('');
  const [formError, setFormError] = useState<string | undefined>();

  const submit = () => {
    if (!title.trim()) {
      setFormError('Informe um título');
      return;
    }
    setFormError(undefined);
    const tagList = tags
      .split(',')
      .map((t) => t.trim())
      .filter(Boolean);
    create.mutate(
      {
        title: title.trim(),
        content: content.trim() || undefined,
        tags: tagList.length ? tagList : undefined,
      },
      {
        onSuccess: () => {
          setTitle('');
          setContent('');
          setTags('');
        },
      },
    );
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Text style={styles.heading}>Notas</Text>

      <Input
        label="Buscar"
        value={search}
        onChangeText={setSearch}
        placeholder="texto ou título"
        testID="notes-search"
      />

      <Card>
        <Input
          label="Título"
          value={title}
          onChangeText={setTitle}
          error={formError}
          testID="notes-title"
        />
        <Input
          label="Conteúdo"
          value={content}
          onChangeText={setContent}
          placeholder="opcional"
          testID="notes-content"
        />
        <Input
          label="Tags"
          value={tags}
          onChangeText={setTags}
          placeholder="separadas, por, vírgula"
          testID="notes-tags"
        />
        <Button
          title="Criar nota"
          onPress={submit}
          loading={create.isPending}
          testID="notes-create"
        />
      </Card>

      {notes.isLoading ? <Text style={styles.muted}>Carregando…</Text> : null}
      {notes.isError ? <Text style={styles.error}>Não foi possível carregar as notas.</Text> : null}
      {notes.data?.length === 0 ? (
        <EmptyState title="Sem notas" hint="Crie a primeira nota acima" />
      ) : null}

      {notes.data?.map((note: Note) => (
        <Card key={note.id} testID={`notes-item-${note.id}`}>
          <Text style={styles.itemTitle}>{note.title}</Text>
          {note.content ? <Text style={styles.body}>{note.content}</Text> : null}
          {note.tags.length ? (
            <View style={styles.tags}>
              {note.tags.map((tag) => (
                <Chip key={tag} label={tag} />
              ))}
            </View>
          ) : null}
          <View style={styles.actions}>
            <Button
              title="Excluir"
              variant="danger"
              onPress={() => remove.mutate(note.id)}
              testID={`notes-remove-${note.id}`}
            />
          </View>
        </Card>
      ))}
    </ScrollView>
  );
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
  tags: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginTop: spacing.sm,
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
