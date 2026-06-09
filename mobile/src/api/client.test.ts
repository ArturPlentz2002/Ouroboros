import { ApiClient, ApiError, TokenProvider } from './client';

interface Captured {
  url: string;
  init: RequestInit;
}

function fakeFetch(response: { ok: boolean; status: number; body: string }) {
  const calls: Captured[] = [];
  const fn = (async (url: unknown, init: unknown) => {
    calls.push({ url: String(url), init: init as RequestInit });
    return {
      ok: response.ok,
      status: response.status,
      text: async () => response.body,
    } as unknown as Response;
  }) as unknown as typeof fetch;
  return { fn, calls };
}

const tokenOf = (t: string | null): TokenProvider => ({ getAccessToken: () => t });

describe('ApiClient', () => {
  it('injeta Authorization quando ha token', async () => {
    const { fn, calls } = fakeFetch({ ok: true, status: 200, body: '{"ok":true}' });
    const client = new ApiClient({ baseUrl: 'http://gw', tokens: tokenOf('abc'), fetchFn: fn });

    await client.get('/api/v1/notes');

    expect(calls[0].url).toBe('http://gw/api/v1/notes');
    const headers = calls[0].init.headers as Record<string, string>;
    expect(headers['Authorization']).toBe('Bearer abc');
  });

  it('omite Authorization quando nao ha token', async () => {
    const { fn, calls } = fakeFetch({ ok: true, status: 200, body: '{}' });
    const client = new ApiClient({ baseUrl: 'http://gw', tokens: tokenOf(null), fetchFn: fn });

    await client.get('/x');

    const headers = calls[0].init.headers as Record<string, string>;
    expect(headers['Authorization']).toBeUndefined();
  });

  it('serializa o body e seta Content-Type no POST', async () => {
    const { fn, calls } = fakeFetch({ ok: true, status: 201, body: '{"id":"1"}' });
    const client = new ApiClient({ baseUrl: 'http://gw', fetchFn: fn });

    const result = await client.post<{ id: string }>('/api/v1/notes', { title: 'Nota' });

    expect(result).toEqual({ id: '1' });
    expect(calls[0].init.method).toBe('POST');
    const headers = calls[0].init.headers as Record<string, string>;
    expect(headers['Content-Type']).toBe('application/json');
    expect(calls[0].init.body).toBe('{"title":"Nota"}');
  });

  it('lanca ApiError com status e corpo em resposta nao-2xx', async () => {
    const { fn } = fakeFetch({ ok: false, status: 404, body: '{"error":"nao encontrado"}' });
    const client = new ApiClient({ baseUrl: 'http://gw', fetchFn: fn });

    await expect(client.get('/x')).rejects.toMatchObject({
      name: 'ApiError',
      status: 404,
      body: { error: 'nao encontrado' },
    });
  });

  it('retorna undefined quando o corpo e vazio (ex.: 204)', async () => {
    const { fn } = fakeFetch({ ok: true, status: 204, body: '' });
    const client = new ApiClient({ baseUrl: 'http://gw', fetchFn: fn });

    await expect(client.delete('/api/v1/notes/1')).resolves.toBeUndefined();
  });

  it('expoe ApiError como instancia de Error', () => {
    expect(new ApiError(500, 'x')).toBeInstanceOf(Error);
  });
});
