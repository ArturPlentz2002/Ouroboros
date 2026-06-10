import { ApiClient } from '../api/client';
import { AuthService } from './authService';
import { InMemoryTokenStore } from './tokenStore';

function clientReturning(body: string, status = 200, ok = true) {
  const calls: { url: string; init: RequestInit }[] = [];
  const fetchFn = (async (url: unknown, init: unknown) => {
    calls.push({ url: String(url), init: init as RequestInit });
    return { ok, status, text: async () => body } as unknown as Response;
  }) as unknown as typeof fetch;
  return { client: new ApiClient({ baseUrl: 'http://gw', fetchFn }), calls };
}

const creds = { email: 'a@b.com', password: 'secret123' };
const tokensJson = '{"accessToken":"acc","refreshToken":"ref"}';

describe('AuthService', () => {
  it('login guarda os tokens e passa a autenticar', async () => {
    const { client, calls } = clientReturning(tokensJson);
    const store = new InMemoryTokenStore();
    const auth = new AuthService(client, store);

    const tokens = await auth.login(creds);

    expect(tokens.accessToken).toBe('acc');
    expect(auth.isAuthenticated()).toBe(true);
    expect(auth.tokenProvider().getAccessToken()).toBe('acc');
    expect(calls[0].url).toBe('http://gw/auth/login');
    expect(calls[0].init.body).toBe(JSON.stringify(creds));
  });

  it('register chama POST /auth/register', async () => {
    const { client, calls } = clientReturning('', 201);
    const auth = new AuthService(client, new InMemoryTokenStore());

    await auth.register(creds);

    expect(calls[0].url).toBe('http://gw/auth/register');
    expect(calls[0].init.method).toBe('POST');
  });

  it('logout limpa a sessao', async () => {
    const { client } = clientReturning(tokensJson);
    const store = new InMemoryTokenStore();
    const auth = new AuthService(client, store);

    await auth.login(creds);
    auth.logout();

    expect(auth.isAuthenticated()).toBe(false);
    expect(auth.tokenProvider().getAccessToken()).toBeNull();
  });
});
