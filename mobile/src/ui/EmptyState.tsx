import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { colors, spacing, typography } from '../design/tokens';

export interface EmptyStateProps {
  title: string;
  hint?: string;
  testID?: string;
}

/** Estado vazio padrao das listas. */
export function EmptyState({ title, hint, testID }: EmptyStateProps): React.ReactElement {
  return (
    <View style={styles.container} testID={testID}>
      <Text style={styles.title}>{title}</Text>
      {hint ? <Text style={styles.hint}>{hint}</Text> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    padding: spacing.xl,
  },
  title: {
    fontSize: typography.fontSizes.subtitle,
    color: colors.text,
    fontWeight: typography.weights.medium,
  },
  hint: {
    marginTop: spacing.xs,
    fontSize: typography.fontSizes.body,
    color: colors.textMuted,
    textAlign: 'center',
  },
});
