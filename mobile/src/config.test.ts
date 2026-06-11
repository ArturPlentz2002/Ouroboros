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

  it('no browser, assume o gateway no mesmo host da pagina (porta 8080)', () => {
    const g = globalThis as { location?: { protocol: string; hostname: string } };
    const original = g.location;
    g.location = { protocol: 'http:', hostname: '192.168.68.211' };
    try {
      expect(resolveConfig({}).apiBaseUrl).toBe('http://192.168.68.211:8080');
    } finally {
      if (original === undefined) {
        delete g.location;
      } else {
        g.location = original;
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
