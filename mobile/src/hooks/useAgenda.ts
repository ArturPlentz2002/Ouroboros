import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useServices } from '../app/ServicesProvider';
import { AgendaEventInput } from '../features/agenda/agendaApi';

const KEY = ['agenda', 'events'];

export function useAgendaEvents() {
  const { agenda } = useServices();
  return useQuery({ queryKey: KEY, queryFn: () => agenda.list() });
}

export function useCreateAgendaEvent() {
  const { agenda } = useServices();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: AgendaEventInput) => agenda.create(input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: KEY }),
  });
}

export function useRemoveAgendaEvent() {
  const { agenda } = useServices();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => agenda.remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: KEY }),
  });
}
