import { InMemoryTokenStore } from './tokenStore';

describe('InMemoryTokenStore', () => {
  it('comeca vazio', () => {
    expect(new InMemoryTokenStore().get()).toBeNull();
  });

  it('guarda e recupera os tokens', () => {
    const store = new InMemoryTokenStore();
    store.set({ accessToken: 'a', refreshToken: 'r' });
    expect(store.get()).toEqual({ accessToken: 'a', refreshToken: 'r' });
  });

  it('limpa os tokens', () => {
    const store = new InMemoryTokenStore();
    store.set({ accessToken: 'a', refreshToken: 'r' });
    store.clear();
    expect(store.get()).toBeNull();
  });
});
