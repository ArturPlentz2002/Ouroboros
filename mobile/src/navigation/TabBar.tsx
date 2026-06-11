import React from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { colors, spacing, typography } from '../design/tokens';
import { TABS, useNavigationStore } from './navigationStore';

/** Barra inferior de abas; reflete e muda a aba ativa do {@link useNavigationStore}. */
export function TabBar(): React.ReactElement {
  const tab = useNavigationStore((s) => s.tab);
  const setTab = useNavigationStore((s) => s.setTab);

  return (
    <View style={styles.bar}>
      {TABS.map((t) => {
        const active = t.key === tab;
        return (
          <Pressable
            key={t.key}
            style={styles.item}
            onPress={() => setTab(t.key)}
            testID={`tab-${t.key}`}
            accessibilityRole="tab"
            accessibilityState={{ selected: active }}
          >
            <Text style={[styles.label, active && styles.labelActive]}>{t.label}</Text>
          </Pressable>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  bar: {
    flexDirection: 'row',
    borderTopWidth: 1,
    borderTopColor: colors.border,
    backgroundColor: colors.background,
  },
  item: {
    flex: 1,
    alignItems: 'center',
    paddingVertical: spacing.md,
  },
  label: {
    fontSize: typography.fontSizes.body,
    color: colors.textMuted,
  },
  labelActive: {
    color: colors.primary,
    fontWeight: typography.weights.bold,
  },
});
