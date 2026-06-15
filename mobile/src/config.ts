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
 * No browser, usa a MESMA origem da pagina (base vazia → caminhos relativos como
 * `/auth/login`). O servidor web (app.web/serve.js) faz proxy de `/auth` e `/api`
 * para o gateway, entao o navegador fala com uma unica porta: funciona em
 * localhost, no IP da rede local e em qualquer dispositivo, sem CORS e sem
 * depender da porta do gateway estar acessivel. Fora do browser, localhost.
 */
function defaultApiBaseUrl(): string {
  const hasWindow = typeof window !== 'undefined' && !!window.location;
  return hasWindow ? '' : 'http://localhost:8080';
}
