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
 * Fetch falso que devolve as respostas informadas em sequencia (repetindo a ultima)
 * e registra as chamadas. Base do {@link fakeHttp} e do harness de testes de telas.
 */
export function fakeFetch(responses: FakeResponse[] = [{}]) {
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
  return { fetchFn, calls };
}

/** Cria um {@link ApiClient} sobre o {@link fakeFetch}. Util para testar os clientes de feature. */
export function fakeHttp(responses: FakeResponse[] = [{}]) {
  const { fetchFn, calls } = fakeFetch(responses);
  const client = new ApiClient({ baseUrl: 'http://gw', fetchFn });
  return { client, calls };
}
