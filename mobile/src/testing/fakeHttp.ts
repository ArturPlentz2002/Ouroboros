import { ApiClient } from '../api/client';

export interface FakeResponse {
  ok?: boolean;
  status?: number;
  body?: string;
}

export interface Captured {
  url: string;
  init: RequestInit;
}

/**
 * Cria um {@link ApiClient} com um fetch falso que devolve as respostas informadas em sequencia
 * (repetindo a ultima) e registra as chamadas. Util para testar os clientes de feature.
 */
export function fakeHttp(responses: FakeResponse[] = [{}]) {
  const calls: Captured[] = [];
  let i = 0;
  const fetchFn = (async (url: unknown, init: unknown) => {
    calls.push({ url: String(url), init: init as RequestInit });
    const r = responses[Math.min(i, responses.length - 1)];
    i++;
    return {
      ok: r.ok ?? true,
      status: r.status ?? 200,
      text: async () => r.body ?? '',
    } as unknown as Response;
  }) as unknown as typeof fetch;
  const client = new ApiClient({ baseUrl: 'http://gw', fetchFn });
  return { client, calls };
}
