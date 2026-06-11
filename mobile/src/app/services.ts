import { ApiClient } from '../api/client';
import { AuthService } from '../auth/authService';
import { InMemoryTokenStore, TokenStore, Tokens } from '../auth/tokenStore';
import { AppConfig, resolveConfig } from '../config';
import { AgendaApi } from '../features/agenda/agendaApi';
import { FinanceApi } from '../features/finance/financeApi';
import { NotesApi } from '../features/notes/notesApi';

/** Raiz de composicao: instancia unica dos clientes/casos de uso consumidos pelas telas. */
export interface AppServices {
  config: AppConfig;
  tokenStore: TokenStore;
  auth: AuthService;
  agenda: AgendaApi;
  finance: FinanceApi;
  notes: NotesApi;
}

const WEB_STORAGE_KEY = 'ouroboros.tokens';

/**
 * TokenStore sobre `localStorage` (alvo web): a sessao sobrevive ao refresh.
 * No alvo nativo sera substituido por armazenamento seguro (Keychain/Keystore).
 */
export class WebStorageTokenStore implements TokenStore {
  constructor(private readonly storage: Storage) {}

  get(): Tokens | null {
    const raw = this.storage.getItem(WEB_STORAGE_KEY);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as Tokens;
    } catch {
      this.storage.removeItem(WEB_STORAGE_KEY);
      return null;
    }
  }

  set(tokens: Tokens): void {
    this.storage.setItem(WEB_STORAGE_KEY, JSON.stringify(tokens));
  }

  clear(): void {
    this.storage.removeItem(WEB_STORAGE_KEY);
  }
}

/** Escolhe o TokenStore conforme o ambiente (localStorage quando disponivel). */
export function createTokenStore(): TokenStore {
  const storage = (globalThis as { localStorage?: Storage }).localStorage;
  return storage ? new WebStorageTokenStore(storage) : new InMemoryTokenStore();
}

export interface CreateServicesOptions {
  config?: AppConfig;
  tokenStore?: TokenStore;
  fetchFn?: typeof fetch;
  /** Chamado quando o refresh falha e a sessao e encerrada (ex.: signOut da UI). */
  onSessionExpired?: () => void;
}

export function createServices(options: CreateServicesOptions = {}): AppServices {
  const config = options.config ?? resolveConfig();
  const tokenStore = options.tokenStore ?? createTokenStore();
  const tokenProvider = { getAccessToken: () => tokenStore.get()?.accessToken ?? null };

  // Client dedicado de auth, SEM onUnauthorized: um 401 no /auth/refresh nao pode
  // disparar outro refresh (recursao/deadlock).
  const authClient = new ApiClient({ baseUrl: config.apiBaseUrl, fetchFn: options.fetchFn });
  const auth = new AuthService(authClient, tokenStore);
  auth.onSessionExpired = options.onSessionExpired;

  const client = new ApiClient({
    baseUrl: config.apiBaseUrl,
    tokens: tokenProvider,
    fetchFn: options.fetchFn,
  });
  client.onUnauthorized = async () => (await auth.refresh()) !== null;

  return {
    config,
    tokenStore,
    auth,
    agenda: new AgendaApi(client),
    finance: new FinanceApi(client),
    notes: new NotesApi(client),
  };
}
