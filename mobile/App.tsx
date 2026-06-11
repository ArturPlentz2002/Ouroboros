import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import React, { useEffect, useMemo, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { ServicesProvider } from './src/app/ServicesProvider';
import { createServices } from './src/app/services';
import { colors, spacing, typography } from './src/design/tokens';
import { AgendaScreen } from './src/features/agenda/AgendaScreen';
import { LoginScreen } from './src/features/auth/LoginScreen';
import { FinanceScreen } from './src/features/finance/FinanceScreen';
import { NotesScreen } from './src/features/notes/NotesScreen';
import { useAuth } from './src/hooks/useAuth';
import { AppNavigator } from './src/navigation/AppNavigator';
import { useSessionStore } from './src/store/sessionStore';

const screens = { agenda: AgendaScreen, finance: FinanceScreen, notes: NotesScreen };

/** Raiz do app: providers + restauro de sessao + header + navegacao. */
export default function App(): React.ReactElement {
  const services = useMemo(
    () => createServices({ onSessionExpired: () => useSessionStore.getState().signOut() }),
    [],
  );
  const [queryClient] = useState(() => new QueryClient());

  // Restaura a sessao persistida (localStorage no alvo web) antes do primeiro render util.
  useEffect(() => {
    if (services.auth.isAuthenticated()) {
      useSessionStore.getState().signIn();
    }
  }, [services]);

  return (
    <QueryClientProvider client={queryClient}>
      <ServicesProvider services={services}>
        <View style={styles.root}>
          <Header />
          <AppNavigator login={LoginScreen} screens={screens} />
        </View>
      </ServicesProvider>
    </QueryClientProvider>
  );
}

function Header(): React.ReactElement | null {
  const authenticated = useSessionStore((s) => s.authenticated);
  const { logout } = useAuth();

  if (!authenticated) {
    return null;
  }
  return (
    <View style={styles.header}>
      <Text style={styles.brand}>Ouroboros</Text>
      <Pressable onPress={logout} testID="app-logout" accessibilityRole="button">
        <Text style={styles.logout}>Sair</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: colors.background,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
  },
  brand: {
    fontSize: typography.fontSizes.subtitle,
    fontWeight: typography.weights.bold,
    color: colors.primary,
  },
  logout: {
    color: colors.textMuted,
    fontSize: typography.fontSizes.body,
  },
});
