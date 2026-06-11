/** @jest-environment jsdom */
import { fireEvent, render, screen } from '@testing-library/react';
import React from 'react';
import { Text } from 'react-native';
import { useSessionStore } from '../store/sessionStore';
import { AppNavigator } from './AppNavigator';
import { useNavigationStore } from './navigationStore';

const Login = () => <Text>tela-login</Text>;
const screens = {
  agenda: () => <Text>tela-agenda</Text>,
  finance: () => <Text>tela-financas</Text>,
  notes: () => <Text>tela-notas</Text>,
};

describe('AppNavigator', () => {
  beforeEach(() => {
    useSessionStore.setState({ authenticated: false });
    useNavigationStore.setState({ tab: 'agenda' });
  });

  it('mostra o login quando deslogado', () => {
    render(<AppNavigator login={Login} screens={screens} />);
    expect(screen.getByText('tela-login')).toBeTruthy();
  });

  it('autenticado, mostra a aba ativa e a TabBar', () => {
    useSessionStore.setState({ authenticated: true });
    render(<AppNavigator login={Login} screens={screens} />);

    expect(screen.getByText('tela-agenda')).toBeTruthy();
    expect(screen.getByTestId('tab-finance')).toBeTruthy();
  });

  it('troca de tela ao tocar na aba', () => {
    useSessionStore.setState({ authenticated: true });
    render(<AppNavigator login={Login} screens={screens} />);

    fireEvent.click(screen.getByTestId('tab-finance'));

    expect(screen.getByText('tela-financas')).toBeTruthy();
    expect(useNavigationStore.getState().tab).toBe('finance');
  });
});
