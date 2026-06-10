/** Par de tokens emitido pelo service-auth. */
export interface Tokens {
  accessToken: string;
  refreshToken: string;
}

/**
 * Armazena os tokens da sessao. Na app real e implementado sobre o armazenamento
 * seguro (Keychain/Keystore); aqui a interface permite uma impl em memoria para testes.
 */
export interface TokenStore {
  get(): Tokens | null;
  set(tokens: Tokens): void;
  clear(): void;
}

/** Implementacao em memoria (testes e fallback de dev). */
export class InMemoryTokenStore implements TokenStore {
  private tokens: Tokens | null = null;

  get(): Tokens | null {
    return this.tokens;
  }

  set(tokens: Tokens): void {
    this.tokens = tokens;
  }

  clear(): void {
    this.tokens = null;
  }
}
