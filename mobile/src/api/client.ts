/** Cliente HTTP do app: injeta o JWT, serializa JSON e traduz erros em {@link ApiError}. */

/** Fornece o access token corrente (ou null quando deslogado). */
export interface TokenProvider {
  getAccessToken(): string | null;
}

/** Erro de uma resposta HTTP nao-2xx. */
export class ApiError extends Error {
  constructor(
    readonly status: number,
    message: string,
    readonly body?: unknown,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export interface ApiClientOptions {
  baseUrl: string;
  tokens?: TokenProvider;
  /** Injetavel para testes; default `globalThis.fetch`. */
  fetchFn?: typeof fetch;
}

export class ApiClient {
  private readonly baseUrl: string;
  private readonly tokens?: TokenProvider;
  private readonly fetchFn: typeof fetch;

  /**
   * Chamado quando uma resposta vem 401; se resolver `true` (credencial renovada),
   * a chamada original e repetida uma unica vez. Ligado em `createServices` ao
   * refresh do AuthService — que usa um client SEM este handler (evita recursao).
   */
  onUnauthorized?: () => Promise<boolean>;

  constructor(options: ApiClientOptions) {
    this.baseUrl = options.baseUrl;
    this.tokens = options.tokens;
    const fn = options.fetchFn ?? globalThis.fetch;
    if (!fn) {
      throw new Error('fetch indisponivel: forneca fetchFn');
    }
    this.fetchFn = fn;
  }

  async request<T>(method: string, path: string, body?: unknown): Promise<T> {
    return this.dispatch<T>(method, path, body, true);
  }

  private async dispatch<T>(
    method: string,
    path: string,
    body: unknown,
    mayRetry: boolean,
  ): Promise<T> {
    const headers: Record<string, string> = { Accept: 'application/json' };

    const token = this.tokens?.getAccessToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    let payload: string | undefined;
    if (body !== undefined) {
      headers['Content-Type'] = 'application/json';
      payload = JSON.stringify(body);
    }

    const res = await this.fetchFn(`${this.baseUrl}${path}`, { method, headers, body: payload });
    const raw = await res.text();
    const data = raw ? JSON.parse(raw) : undefined;

    if (res.status === 401 && mayRetry && this.onUnauthorized) {
      const renewed = await this.onUnauthorized();
      if (renewed) {
        return this.dispatch<T>(method, path, body, false);
      }
    }
    if (!res.ok) {
      throw new ApiError(res.status, `HTTP ${res.status} em ${method} ${path}`, data);
    }
    return data as T;
  }

  get<T>(path: string): Promise<T> {
    return this.request<T>('GET', path);
  }

  post<T>(path: string, body?: unknown): Promise<T> {
    return this.request<T>('POST', path, body);
  }

  put<T>(path: string, body?: unknown): Promise<T> {
    return this.request<T>('PUT', path, body);
  }

  delete<T>(path: string): Promise<T> {
    return this.request<T>('DELETE', path);
  }
}
