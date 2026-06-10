import { ApiClient, TokenProvider } from '../api/client';
import { TokenStore, Tokens } from './tokenStore';

export interface Credentials {
  email: string;
  password: string;
}

/**
 * Casos de uso de autenticacao sobre o {@link ApiClient} e o {@link TokenStore}.
 * Apos o login, os tokens ficam no store e sao injetados via {@link tokenProvider}.
 */
export class AuthService {
  constructor(
    private readonly client: ApiClient,
    private readonly store: TokenStore,
  ) {}

  async register(credentials: Credentials): Promise<void> {
    await this.client.post<void>('/auth/register', credentials);
  }

  async login(credentials: Credentials): Promise<Tokens> {
    const tokens = await this.client.post<Tokens>('/auth/login', credentials);
    this.store.set(tokens);
    return tokens;
  }

  logout(): void {
    this.store.clear();
  }

  isAuthenticated(): boolean {
    return this.store.get() !== null;
  }

  /** Provider para o {@link ApiClient} ler o access token corrente. */
  tokenProvider(): TokenProvider {
    return { getAccessToken: () => this.store.get()?.accessToken ?? null };
  }
}
