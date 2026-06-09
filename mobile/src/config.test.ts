import { resolveConfig } from './config';

describe('resolveConfig', () => {
  it('usa o gateway local por padrao', () => {
    expect(resolveConfig({}).apiBaseUrl).toBe('http://localhost:8080');
  });

  it('respeita OUROBOROS_API_URL quando definido', () => {
    expect(resolveConfig({ OUROBOROS_API_URL: 'https://api.ouroboros.app' }).apiBaseUrl).toBe(
      'https://api.ouroboros.app',
    );
  });
});
