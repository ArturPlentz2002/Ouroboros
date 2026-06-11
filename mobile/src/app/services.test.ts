import { InMemoryTokenStore } from '../auth/tokenStore';
import { fakeFetch } from '../testing/fakeHttp';
import { WebStorageTokenStore, createServices, createTokenStore } from './services';

function fakeStorage(): Storage {
  const data = new Map<string, string>();
  return {
    getItem: (k: string) => data.get(k) ?? null,
    setItem: (k: string, v: string) => void data.set(k, v),
    removeItem: (k: string) => void data.delete(k),
    clear: () => data.clear(),
    key: () => null,
    get length() {
      return data.size;
    },
  } as Storage;
}

describe('WebStorageTokenStore', () => {
  it('persiste e limpa tokens', () => {
    const store = new WebStorageTokenStore(fakeStorage());
    expect(store.get()).toBeNull();

    store.set({ accessToken: 'a', refreshToken: 'r' });
    expect(store.get()).toEqual({ accessToken: 'a', refreshToken: 'r' });

    store.clear();
    expect(store.get()).toBeNull();
  });

  it('descarta conteudo corrompido', () => {
    const storage = fakeStorage();
    storage.setItem('ouroboros.tokens', '{nao-e-json');
    const store = new WebStorageTokenStore(storage);

    expect(store.get()).toBeNull();
    expect(storage.getItem('ouroboros.tokens')).toBeNull();
  });
});

describe('createTokenStore', () => {
  it('cai para memoria quando nao ha localStorage (ambiente node)', () => {
    expect(createTokenStore()).toBeInstanceOf(InMemoryTokenStore);
  });
});

describe('createServices', () => {
  it('injeta o token do store nas chamadas dos clientes', async () => {
    const { fetchFn, calls } = fakeFetch([{ body: '[]' }]);
    const tokenStore = new InMemoryTokenStore();
    tokenStore.set({ accessToken: 'jwt-abc', refreshToken: 'r' });
    const services = createServices({
      config: { apiBaseUrl: 'http://gw' },
      tokenStore,
      fetchFn,
    });

    await services.agenda.list();

    const headers = calls[0].init.headers as Record<string, string>;
    expect(headers['Authorization']).toBe('Bearer jwt-abc');
  });
});
