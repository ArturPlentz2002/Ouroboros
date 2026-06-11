import { create } from 'zustand';

/** Estado global da sessao: a UI reage a ele; quem o muda sao os hooks de auth. */
export interface SessionState {
  authenticated: boolean;
  signIn(): void;
  signOut(): void;
}

export const useSessionStore = create<SessionState>((set) => ({
  authenticated: false,
  signIn: () => set({ authenticated: true }),
  signOut: () => set({ authenticated: false }),
}));
