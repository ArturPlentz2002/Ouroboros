import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { resolveConfig } from './src/config';
import { colors, spacing, typography } from './src/design/tokens';

/** Raiz compartilhada do app (mesma base para mobile e web via React Native Web). */
export default function App(): React.ReactElement {
  const config = resolveConfig();
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Ouroboros</Text>
      <Text style={styles.subtitle}>Organizador pessoal</Text>
      <Text style={styles.api}>API: {config.apiBaseUrl}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.background,
    padding: spacing.lg,
  },
  title: {
    fontSize: typography.fontSizes.heading,
    fontWeight: '700',
    color: colors.primary,
  },
  subtitle: {
    fontSize: typography.fontSizes.subtitle,
    color: colors.textMuted,
    marginTop: spacing.sm,
  },
  api: {
    fontSize: typography.fontSizes.caption,
    color: colors.textMuted,
    marginTop: spacing.lg,
  },
});
