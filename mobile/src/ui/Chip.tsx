import React from 'react';
import { Pressable, StyleSheet, Text } from 'react-native';
import { colors, radii, spacing, typography } from '../design/tokens';

export interface ChipProps {
  label: string;
  /** Quando presente, o chip e tocavel (filtros/selecao). */
  onPress?(): void;
  selected?: boolean;
  testID?: string;
}

/** Marcador compacto para tags e filtros. */
export function Chip({ label, onPress, selected = false, testID }: ChipProps): React.ReactElement {
  return (
    <Pressable
      style={[styles.chip, selected && styles.chipSelected]}
      onPress={onPress}
      disabled={!onPress}
      testID={testID}
      accessibilityState={{ selected }}
    >
      <Text style={[styles.label, selected && styles.labelSelected]}>{label}</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  chip: {
    borderRadius: radii.pill,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.surface,
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs,
    marginRight: spacing.xs,
    marginBottom: spacing.xs,
    alignSelf: 'flex-start',
  },
  chipSelected: {
    backgroundColor: colors.primary,
    borderColor: colors.primary,
  },
  label: {
    fontSize: typography.fontSizes.caption,
    color: colors.text,
  },
  labelSelected: {
    color: '#fff',
    fontWeight: typography.weights.medium,
  },
});
