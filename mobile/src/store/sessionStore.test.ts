import { useSessionStore } from './sessionStore';

describe('sessionStore', () => {
  beforeEach(() => useSessionStore.setState({ authenticated: false }));

  it('inicia deslogado', () => {
    expect(useSessionStore.getState().authenticated).toBe(false);
  });

  it('signIn marca autenticado e signOut desfaz', () => {
    useSessionStore.getState().signIn();
    expect(useSessionStore.getState().authenticated).toBe(true);

    useSessionStore.getState().signOut();
    expect(useSessionStore.getState().authenticated).toBe(false);
  });
});
