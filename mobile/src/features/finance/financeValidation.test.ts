import { isValidIsoDate, parseAmount } from './FinanceScreen';

describe('parseAmount', () => {
  it('aceita inteiro e decimais com virgula ou ponto', () => {
    expect(parseAmount('1000')).toBe(1000);
    expect(parseAmount('150,50')).toBe(150.5);
    expect(parseAmount('150.5')).toBe(150.5);
  });

  it('rejeita separador de milhar ("1.000" viraria R$ 1,00)', () => {
    expect(parseAmount('1.000')).toBeNull();
    expect(parseAmount('1.000,50')).toBeNull();
  });

  it('rejeita zero, negativo e lixo', () => {
    expect(parseAmount('0')).toBeNull();
    expect(parseAmount('-5')).toBeNull();
    expect(parseAmount('abc')).toBeNull();
    expect(parseAmount('')).toBeNull();
  });
});

describe('isValidIsoDate', () => {
  it('aceita data de calendario real', () => {
    expect(isValidIsoDate('2026-06-11')).toBe(true);
    expect(isValidIsoDate('2024-02-29')).toBe(true); // bissexto
  });

  it('rejeita mes/dia impossiveis e formatos errados', () => {
    expect(isValidIsoDate('2026-13-45')).toBe(false);
    expect(isValidIsoDate('2026-02-30')).toBe(false);
    expect(isValidIsoDate('11/06/2026')).toBe(false);
    expect(isValidIsoDate('')).toBe(false);
  });
});
