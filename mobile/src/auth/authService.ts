import { ApiClient, TokenProvider } from '../api/client';
import { TokenStore, Tokens } from './tokenStore';

export interface Credentials {
  email: string;
  password: string;
}

/**
 * Casos de uso de autenticacao sobre o {@link ApiClient} e o {@link TokenStore}.
 * Apos o login, os tokens ficam no store e sao injetados via {@link tokenProvider}.
 *
 * IMPORTANTE: o client recebido aqui NAO deve ter `onUnauthorized` apontando para
 * este servico (recursao); `createServices` usa um client dedicado para auth.
 */
export class AuthService {
  /** Notificado quando o refresh falha e a sessao local e encerrada. */
  onSessionExpired?: () => void;

  private refreshing: Promise<Tokens | null> | null = null;

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

  /**
   * Troca o refresh token por um novo par (rotacao). Single-flight: chamadas
   * concorrentes compartilham a mesma requisicao — o backend invalida o token
   * usado, entao um segundo refresh paralelo falharia e derrubaria a sessao.
   * Na falha, limpa a sessao local e notifica {@link onSessionExpired}.
   */
  refresh(): Promise<Tokens | null> {
    if (!this.refreshing) {
      this.refreshing = this.doRefresh().finally(() => {
        this.refreshing = null;
      });
    }
    return this.refreshing;
  }

  private async doRefresh(): Promise<Tokens | null> {
    const current = this.store.get();
    if (!current) {
      return null;
    }
    try {
      const tokens = await this.client.post<Tokens>('/auth/refresh', {
        refreshToken: current.refreshToken,
      });
      this.store.set(tokens);
      return tokens;
    } catch {
      this.store.clear();
      this.onSessionExpired?.();
      return null;
    }
  }

  /** Revoga o refresh token no servidor (melhor esforco) e limpa a sessao local. */
  logout(): void {
    const tokens = this.store.get();
    if (tokens) {
      this.client.post<void>('/auth/logout', { refreshToken: tokens.refreshToken }).catch(() => {
        // logout local segue mesmo se a revogacao remota falhar (offline etc.)
      });
    }
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
