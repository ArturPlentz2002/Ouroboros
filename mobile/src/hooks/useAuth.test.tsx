/** @jest-environment jsdom */
import { renderHook, waitFor } from '@testing-library/react';
import { useSessionStore } from '../store/sessionStore';
import { testHarness } from '../testing/renderWithProviders';
import { useAuth } from './useAuth';

const tokensJson = '{"accessToken":"jwt-a","refreshToken":"jwt-r"}';

describe('useAuth', () => {
  beforeEach(() => useSessionStore.setState({ authenticated: false }));

  it('login guarda tokens e marca a sessao como autenticada', async () => {
    const harness = testHarness([{ body: tokensJson }]);
    const { result } = renderHook(() => useAuth(), { wrapper: harness.wrapper });

    result.current.login.mutate({ email: 'a@b.c', password: 'segredo1' });
    await waitFor(() => expect(result.current.login.isSuccess).toBe(true));

    expect(useSessionStore.getState().authenticated).toBe(true);
    expect(harness.services.tokenStore.get()?.accessToken).toBe('jwt-a');
    expect(harness.calls[0].url).toBe('http://gw/auth/login');
  });

  it('register cria a conta e ja faz login', async () => {
    const harness = testHarness([{ status: 201, body: '' }, { body: tokensJson }]);
    const { result } = renderHook(() => useAuth(), { wrapper: harness.wrapper });

    result.current.register.mutate({ email: 'a@b.c', password: 'segredo1' });
    await waitFor(() => expect(result.current.register.isSuccess).toBe(true));

    expect(harness.calls[0].url).toBe('http://gw/auth/register');
    expect(harness.calls[1].url).toBe('http://gw/auth/login');
    expect(useSessionStore.getState().authenticated).toBe(true);
  });

  it('logout limpa tokens e sessao', async () => {
    const harness = testHarness([{ body: tokensJson }]);
    const { result } = renderHook(() => useAuth(), { wrapper: harness.wrapper });

    result.current.login.mutate({ email: 'a@b.c', password: 'segredo1' });
    await waitFor(() => expect(useSessionStore.getState().authenticated).toBe(true));

    result.current.logout();

    expect(useSessionStore.getState().authenticated).toBe(false);
    expect(harness.services.tokenStore.get()).toBeNull();
  });

  it('login com erro mantem a sessao deslogada', async () => {
    const harness = testHarness([{ ok: false, status: 401, body: '{"message":"bad"}' }]);
    const { result } = renderHook(() => useAuth(), { wrapper: harness.wrapper });

    result.current.login.mutate({ email: 'a@b.c', password: 'errada' });
    await waitFor(() => expect(result.current.login.isError).toBe(true));

    expect(useSessionStore.getState().authenticated).toBe(false);
  });
});
