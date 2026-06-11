import React from 'react';
import { ActivityIndicator, Pressable, StyleSheet, Text } from 'react-native';
import { colors, radii, spacing, typography } from '../design/tokens';

export type ButtonVariant = 'primary' | 'ghost' | 'danger';

export interface ButtonProps {
  title: string;
  onPress(): void;
  variant?: ButtonVariant;
  disabled?: boolean;
  /** Mostra spinner e bloqueia toques (estados de envio). */
  loading?: boolean;
  testID?: string;
}

/** Botao do design system; altura minima 44 para area de toque acessivel. */
export function Button({
  title,
  onPress,
  variant = 'primary',
  disabled = false,
  loading = false,
  testID,
}: ButtonProps): React.ReactElement {
  const blocked = disabled || loading;
  return (
    <Pressable
      style={[styles.base, variantStyles[variant], blocked && styles.disabled]}
      onPress={blocked ? undefined : onPress}
      disabled={blocked}
      testID={testID}
      accessibilityRole="button"
    >
      {loading ? (
        <ActivityIndicator size="small" color={variant === 'ghost' ? colors.primary : '#fff'} />
      ) : (
        <Text style={[styles.label, variant === 'ghost' && styles.labelGhost]}>{title}</Text>
      )}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  base: {
    minHeight: 44,
    borderRadius: radii.md,
    paddingHorizontal: spacing.md,
    alignItems: 'center',
    justifyContent: 'center',
  },
  disabled: {
    opacity: 0.5,
  },
  label: {
    color: '#fff',
    fontSize: typography.fontSizes.subtitle,
    fontWeight: typography.weights.medium,
  },
  labelGhost: {
    color: colors.primary,
  },
});

const variantStyles = StyleSheet.create({
  primary: { backgroundColor: colors.primary },
  danger: { backgroundColor: colors.danger },
  ghost: { backgroundColor: 'transparent' },
});
