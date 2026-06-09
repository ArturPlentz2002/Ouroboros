# Ouroboros Mobile

App React Native (TypeScript) do Ouroboros. Esta fase inicial entrega a base da
camada de logica do app (configuracao, futuramente cliente de API, store de auth,
design tokens), testada com Jest + ts-jest em Node — sem depender de emulador.

## Comandos

```bash
npm ci          # instala as dependencias (usa o package-lock.json)
npm test        # roda os testes (Jest)
npm run typecheck  # checagem de tipos (tsc)
```

A UI React Native (componentes/telas com NativeWind + design system) e adicionada
nas tasks seguintes da Fase F.
