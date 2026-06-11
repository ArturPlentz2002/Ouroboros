import { create } from 'zustand';

/** Abas raiz do app autenticado. */
export type TabKey = 'agenda' | 'finance' | 'notes';

export const TABS: ReadonlyArray<{ key: TabKey; label: string }> = [
  { key: 'agenda', label: 'Agenda' },
  { key: 'finance', label: 'Finanças' },
  { key: 'notes', label: 'Notas' },
];

export interface NavigationState {
  tab: TabKey;
  setTab(tab: TabKey): void;
}

/** Estado de navegação minimalista (abas planas). React Navigation entra com o alvo nativo. */
export const useNavigationStore = create<NavigationState>((set) => ({
  tab: 'agenda',
  setTab: (tab) => set({ tab }),
}));
