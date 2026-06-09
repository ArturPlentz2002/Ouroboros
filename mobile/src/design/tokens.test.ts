import { colors, spacing, typography } from './tokens';

const HEX = /^#[0-9a-fA-F]{6}$/;

describe('design tokens', () => {
  it('todas as cores sao hex de 6 digitos', () => {
    for (const [name, value] of Object.entries(colors)) {
      expect(value).toMatch(HEX);
      expect(name).not.toHaveLength(0);
    }
  });

  it('a escala de espacamento e estritamente crescente', () => {
    const values = Object.values(spacing);
    for (let i = 1; i < values.length; i++) {
      expect(values[i]).toBeGreaterThan(values[i - 1]);
    }
  });

  it('os tamanhos de fonte sao estritamente crescentes', () => {
    const sizes = Object.values(typography.fontSizes);
    for (let i = 1; i < sizes.length; i++) {
      expect(sizes[i]).toBeGreaterThan(sizes[i - 1]);
    }
  });
});
