import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useServices } from '../app/ServicesProvider';
import { FinanceEntryInput } from '../features/finance/financeApi';

const ENTRIES_KEY = ['finance', 'entries'];
const SUMMARY_KEY = ['finance', 'summary'];
const CATEGORIES_KEY = ['finance', 'categories'];

export function useFinanceEntries() {
  const { finance } = useServices();
  return useQuery({ queryKey: ENTRIES_KEY, queryFn: () => finance.listEntries() });
}

/** Resumo do mes (`YYYY-MM`); sem argumento, o mes atual do backend. */
export function useMonthlySummary(month?: string) {
  const { finance } = useServices();
  return useQuery({
    queryKey: [...SUMMARY_KEY, month ?? 'current'],
    queryFn: () => finance.summary(month),
  });
}

export function useFinanceCategories() {
  const { finance } = useServices();
  return useQuery({ queryKey: CATEGORIES_KEY, queryFn: () => finance.listCategories() });
}

export function useCreateFinanceEntry() {
  const { finance } = useServices();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: FinanceEntryInput) => finance.createEntry(input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ENTRIES_KEY });
      queryClient.invalidateQueries({ queryKey: SUMMARY_KEY });
    },
  });
}

export function useRemoveFinanceEntry() {
  const { finance } = useServices();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => finance.removeEntry(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ENTRIES_KEY });
      queryClient.invalidateQueries({ queryKey: SUMMARY_KEY });
    },
  });
}
