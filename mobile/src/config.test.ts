import { resolveConfig } from './config';

describe('resolveConfig', () => {
  it('usa o gateway local por padrao (fora do browser)', () => {
    expect(resolveConfig({}).apiBaseUrl).toBe('http://localhost:8080');
  });

  it('respeita OUROBOROS_API_URL quando definido', () => {
    expect(resolveConfig({ OUROBOROS_API_URL: 'https://api.ouroboros.app' }).apiBaseUrl).toBe(
      'https://api.ouroboros.app',
    );
  });

  it('no browser, usa a mesma origem (base vazia → caminhos relativos, proxy no servidor)', () => {
    const g = globalThis as { window?: unknown };
    const original = g.window;
    g.window = { location: { protocol: 'http:', hostname: '192.168.1.6' } };
    try {
      expect(resolveConfig({}).apiBaseUrl).toBe('');
    } finally {
      if (original === undefined) {
        delete g.window;
      } else {
        g.window = original;
      }
    }
  });

  it('nao referencia process sem guarda (boot do app web)', () => {
    // Sem env explicito e sem process: nao pode lancar ReferenceError.
    const descriptor = Object.getOwnPropertyDescriptor(globalThis, 'process');
    // Em Node nao da para remover process de verdade; o teste de regressao real
    // e o typecheck + o guard `typeof process !== 'undefined'` em config.ts.
    expect(descriptor).toBeDefined();
    expect(() => resolveConfig()).not.toThrow();
  });
});
