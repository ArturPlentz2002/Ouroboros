/** Configuracao do app, resolvida a partir do ambiente. */
export interface AppConfig {
  /** URL base da API (gateway). */
  apiBaseUrl: string;
}

/** Valor injetado pelo DefinePlugin no build web (string) ou ausente (Node/teste). */
declare const __OUROBOROS_API_URL__: string | null | undefined;

/**
 * Resolve a configuracao. Ordem: `env` explicito (testes) > process.env (Node) >
 * valor injetado no build web > default por ambiente.
 *
 * IMPORTANTE: `process` nao existe no browser — todo acesso e guardado por
 * `typeof`; referencia-lo direto quebra o boot do app web (tela branca).
 */
export function resolveConfig(env?: Record<string, string | undefined>): AppConfig {
  const nodeEnv = env ?? (typeof process !== 'undefined' ? process.env : undefined);
  return {
    apiBaseUrl: nodeEnv?.OUROBOROS_API_URL ?? buildTimeApiUrl() ?? defaultApiBaseUrl(),
  };
}

function buildTimeApiUrl(): string | undefined {
  return typeof __OUROBOROS_API_URL__ === 'string' && __OUROBOROS_API_URL__
    ? __OUROBOROS_API_URL__
    : undefined;
}

/**
 * No browser, assume o gateway no mesmo host da pagina (porta 8080) — assim o
 * app servido via IP da rede local (ex.: http://192.168.x.y:8081) fala com o
 * gateway certo sem configuracao. Fora do browser, localhost.
 */
function defaultApiBaseUrl(): string {
  const location = (globalThis as { location?: { protocol: string; hostname: string } }).location;
  if (location?.hostname) {
    return `${location.protocol}//${location.hostname}:8080`;
  }
  return 'http://localhost:8080';
}
