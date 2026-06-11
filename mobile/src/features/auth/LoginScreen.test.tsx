/** @jest-environment jsdom */
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import React from 'react';
import { useSessionStore } from '../../store/sessionStore';
import { testHarness } from '../../testing/renderWithProviders';
import { LoginScreen } from './LoginScreen';

const tokensJson = '{"accessToken":"jwt-a","refreshToken":"jwt-r"}';

function fill(email: string, password: string) {
  fireEvent.change(screen.getByTestId('login-email'), { target: { value: email } });
  fireEvent.change(screen.getByTestId('login-password'), { target: { value: password } });
}

describe('LoginScreen', () => {
  beforeEach(() => useSessionStore.setState({ authenticated: false }));

  it('faz login e autentica a sessao', async () => {
    const harness = testHarness([{ body: tokensJson }]);
    render(<LoginScreen />, { wrapper: harness.wrapper });

    fill('a@b.c', 'segredo123');
    fireEvent.click(screen.getByTestId('login-submit'));

    await waitFor(() => expect(useSessionStore.getState().authenticated).toBe(true));
    expect(harness.calls[0].url).toBe('http://gw/auth/login');
    expect(harness.calls[0].init.body).toBe('{"email":"a@b.c","password":"segredo123"}');
  });

  it('mostra erro de credenciais invalidas', async () => {
    const harness = testHarness([{ ok: false, status: 401, body: '{}' }]);
    render(<LoginScreen />, { wrapper: harness.wrapper });

    fill('a@b.c', 'errada123');
    fireEvent.click(screen.getByTestId('login-submit'));

    await waitFor(() => expect(screen.getByText('E-mail ou senha incorretos')).toBeTruthy());
    expect(useSessionStore.getState().authenticated).toBe(false);
  });

  it('alterna para cadastro e registra + entra', async () => {
    const harness = testHarness([{ status: 201, body: '' }, { body: tokensJson }]);
    render(<LoginScreen />, { wrapper: harness.wrapper });

    fireEvent.click(screen.getByTestId('login-toggle'));
    fill('novo@b.c', 'segredo123');
    fireEvent.click(screen.getByTestId('login-submit'));

    await waitFor(() => expect(useSessionStore.getState().authenticated).toBe(true));
    expect(harness.calls[0].url).toBe('http://gw/auth/register');
    expect(harness.calls[1].url).toBe('http://gw/auth/login');
  });

  it('nao envia com campos vazios', () => {
    const harness = testHarness([]);
    render(<LoginScreen />, { wrapper: harness.wrapper });

    fireEvent.click(screen.getByTestId('login-submit'));
    expect(harness.calls).toHaveLength(0);
  });
});
