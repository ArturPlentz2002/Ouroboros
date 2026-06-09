/** Configuracao do app, resolvida a partir do ambiente. */
export interface AppConfig {
  /** URL base da API (gateway). */
  apiBaseUrl: string;
}

const DEFAULT_API_BASE_URL = 'http://localhost:8080';

/** Resolve a configuracao, com defaults para desenvolvimento local. */
export function resolveConfig(env: Record<string, string | undefined> = process.env): AppConfig {
  return {
    apiBaseUrl: env.OUROBOROS_API_URL ?? DEFAULT_API_BASE_URL,
  };
}
