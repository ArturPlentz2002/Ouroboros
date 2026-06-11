import React from 'react';
import { StyleSheet, View } from 'react-native';
import { colors } from '../design/tokens';
import { useSessionStore } from '../store/sessionStore';
import { TabBar } from './TabBar';
import { TabKey, useNavigationStore } from './navigationStore';

export interface AppNavigatorProps {
  /** Tela exibida enquanto a sessao nao esta autenticada. */
  login: React.ComponentType;
  /** Tela de cada aba; injetadas para o navegador nao depender das features. */
  screens: Record<TabKey, React.ComponentType>;
}

/** Porta de entrada do app: gate de autenticacao + abas planas. */
export function AppNavigator({ login: Login, screens }: AppNavigatorProps): React.ReactElement {
  const authenticated = useSessionStore((s) => s.authenticated);
  const tab = useNavigationStore((s) => s.tab);

  if (!authenticated) {
    return <Login />;
  }

  const Screen = screens[tab];
  return (
    <View style={styles.container}>
      <View style={styles.content}>
        <Screen />
      </View>
      <TabBar />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  content: {
    flex: 1,
  },
});
