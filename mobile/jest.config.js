/**
 * Testes do app: logica pura em Node (ts-jest) e componentes/hooks em jsdom
 * (arquivos .test.tsx declaram `@jest-environment jsdom` no docblock).
 */
module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  roots: ['<rootDir>/src'],
  testMatch: ['**/*.test.ts', '**/*.test.tsx'],
  moduleNameMapper: {
    // No alvo web, `react-native` e servido pelo react-native-web (igual ao webpack).
    '^react-native$': 'react-native-web',
  },
  collectCoverageFrom: ['src/**/*.{ts,tsx}', '!src/**/*.test.{ts,tsx}'],
};
