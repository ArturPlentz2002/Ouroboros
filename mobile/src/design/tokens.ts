/**
 * Design tokens do Ouroboros: fonte unica de cores, espacamentos, raios e tipografia.
 * Consumidos pelos componentes do design system (UI nas tasks seguintes).
 */

export const colors = {
  primary: '#4f46e5',
  primaryDark: '#3730a3',
  background: '#ffffff',
  surface: '#f5f5f7',
  text: '#111827',
  textMuted: '#6b7280',
  border: '#e5e7eb',
  danger: '#dc2626',
  success: '#16a34a',
  warning: '#d97706',
} as const;

/** Escala de espacamento (em pontos), base 4. */
export const spacing = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
  xxl: 48,
} as const;

export const radii = {
  sm: 4,
  md: 8,
  lg: 16,
  pill: 999,
} as const;

export const typography = {
  fontSizes: {
    caption: 12,
    body: 14,
    subtitle: 16,
    title: 20,
    heading: 28,
  },
  weights: {
    regular: '400',
    medium: '500',
    bold: '700',
  },
} as const;

export const tokens = { colors, spacing, radii, typography } as const;

export type Tokens = typeof tokens;
export type ColorName = keyof typeof colors;
export type SpacingName = keyof typeof spacing;
