import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useServices } from '../app/ServicesProvider';
import { NoteInput, NoteQuery } from '../features/notes/notesApi';

const KEY = ['notes'];

export function useNotes(query: NoteQuery = {}) {
  const { notes } = useServices();
  return useQuery({
    queryKey: [...KEY, query.tag ?? '', query.q ?? ''],
    queryFn: () => notes.list(query),
  });
}

export function useCreateNote() {
  const { notes } = useServices();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: NoteInput) => notes.create(input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: KEY }),
  });
}

export function useRemoveNote() {
  const { notes } = useServices();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => notes.remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: KEY }),
  });
}
