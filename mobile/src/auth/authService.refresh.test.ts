import { ApiClient, ApiError } from '../api/client';
import { fakeFetch } from '../testing/fakeHttp';
import { AuthService } from './authService';
import { InMemoryTokenStore } from './tokenStore';

const tokensJson = '{"accessToken":"novo-acc","refreshToken":"novo-ref"}';

function authWith(responses: Parameters<typeof fakeFetch>[0]) {
  const { fetchFn, calls } = fakeFetch(responses);
  const store = new InMemoryTokenStore();
  store.set({ accessToken: 'acc', refreshToken: 'ref' });
  const auth = new AuthService(new ApiClient({ baseUrl: 'http://gw', fetchFn }), store);
  return { auth, store, calls };
}

describe('AuthService.refresh', () => {
  it('troca o refresh token pelo novo par e atualiza o store', async () => {
    const { auth, store, calls } = authWith([{ body: tokensJson }]);

    const tokens = await auth.refresh();

    expect(tokens?.accessToken).toBe('novo-acc');
    expect(store.get()?.refreshToken).toBe('novo-ref');
    expect(calls[0].url).toBe('http://gw/auth/refresh');
    expect(calls[0].init.body).toBe('{"refreshToken":"ref"}');
  });

  it('na falha limpa a sessao e notifica onSessionExpired', async () => {
    const { auth, store } = authWith([{ ok: false, status: 401, body: '{}' }]);
    const expired = jest.fn();
    auth.onSessionExpired = expired;

    const tokens = await auth.refresh();

    expect(tokens).toBeNull();
    expect(store.get()).toBeNull();
    expect(expired).toHaveBeenCalledTimes(1);
  });

  it('sem sessao, retorna null sem chamar a API', async () => {
    const { auth, store, calls } = authWith([{ body: tokensJson }]);
    store.clear();

    expect(await auth.refresh()).toBeNull();
    expect(calls).toHaveLength(0);
  });

  it('single-flight: chamadas concorrentes compartilham uma unica requisicao', async () => {
    const { auth, calls } = authWith([{ body: tokensJson }]);

    const [a, b] = await Promise.all([auth.refresh(), auth.refresh()]);

    expect(a?.accessToken).toBe('novo-acc');
    expect(b?.accessToken).toBe('novo-acc');
    expect(calls).toHaveLength(1);
  });
});

describe('AuthService.logout', () => {
  it('revoga o refresh token no servidor e limpa a sessao local', async () => {
    const { auth, store, calls } = authWith([{ status: 204, body: '' }]);

    auth.logout();

    expect(store.get()).toBeNull();
    expect(calls[0].url).toBe('http://gw/auth/logout');
    expect(calls[0].init.body).toBe('{"refreshToken":"ref"}');
  });

  it('segue com o logout local mesmo se a revogacao remota falhar', async () => {
    const { auth, store } = authWith([{ ok: false, status: 500, body: '' }]);

    auth.logout();
    await Promise.resolve();

    expect(store.get()).toBeNull();
  });
});

describe('ApiClient.onUnauthorized', () => {
  it('renova num 401 e repete a chamada original uma vez', async () => {
    const { fetchFn, calls } = fakeFetch([
      { ok: false, status: 401, body: '' },
      { body: '[]' },
    ]);
    let token = 'velho';
    const client = new ApiClient({
      baseUrl: 'http://gw',
      tokens: { getAccessToken: () => token },
      fetchFn,
    });
    client.onUnauthorized = async () => {
      token = 'renovado';
      return true;
    };

    const data = await client.get<unknown[]>('/api/v1/agenda/events');

    expect(data).toEqual([]);
    expect(calls).toHaveLength(2);
    expect((calls[1].init.headers as Record<string, string>)['Authorization']).toBe(
      'Bearer renovado',
    );
  });

  it('quando a renovacao falha, propaga o 401 original sem repetir de novo', async () => {
    const { fetchFn, calls } = fakeFetch([{ ok: false, status: 401, body: '' }]);
    const client = new ApiClient({ baseUrl: 'http://gw', fetchFn });
    client.onUnauthorized = async () => false;

    await expect(client.get('/x')).rejects.toThrow(ApiError);
    expect(calls).toHaveLength(1);
  });

  it('um 401 na repeticao nao dispara nova renovacao (sem loop)', async () => {
    const { fetchFn, calls } = fakeFetch([{ ok: false, status: 401, body: '' }]);
    const client = new ApiClient({ baseUrl: 'http://gw', fetchFn });
    const handler = jest.fn(async () => true);
    client.onUnauthorized = handler;

    await expect(client.get('/x')).rejects.toThrow(ApiError);

    expect(handler).toHaveBeenCalledTimes(1);
    expect(calls).toHaveLength(2);
  });
});

describe('integracao via createServices', () => {
  it('um 401 numa chamada de feature renova a sessao e repete', async () => {
    // ordem: agenda 401 -> refresh 200 -> agenda 200
    const { fetchFn, calls } = fakeFetch([
      { ok: false, status: 401, body: '' },
      { body: tokensJson },
      { body: '[]' },
    ]);
    const store = new InMemoryTokenStore();
    store.set({ accessToken: 'acc', refreshToken: 'ref' });
    // import circular evitado: createServices importado aqui dentro
    const { createServices } = await import('../app/services');
    const services = createServices({
      config: { apiBaseUrl: 'http://gw' },
      tokenStore: store,
      fetchFn,
    });

    const events = await services.agenda.list();

    expect(events).toEqual([]);
    expect(calls.map((c) => c.url)).toEqual([
      'http://gw/api/v1/agenda/events',
      'http://gw/auth/refresh',
      'http://gw/api/v1/agenda/events',
    ]);
    expect(store.get()?.accessToken).toBe('novo-acc');
  });
});
