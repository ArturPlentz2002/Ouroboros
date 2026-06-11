# Ouroboros Mobile

App do Ouroboros em React Native + TypeScript, rodando hoje no **alvo web** via
React Native Web (o alvo nativo — RN CLI — entra depois e reutiliza todo o código).

MVP atual: login/cadastro, agenda (CRUD de eventos), finanças (lançamentos +
resumo do mês) e notas (busca + tags), consumindo a API pelo gateway.

## Comandos

```bash
npm ci             # instala as dependencias (usa o package-lock.json)
npm test           # roda os testes (Jest; logica em Node, telas em jsdom)
npm run typecheck  # checagem de tipos (tsc)
npm run web        # dev server web em http://localhost:8081
npm run build:web  # build de producao em app.web/dist/
```

A API alvo vem de `OUROBOROS_API_URL` (default `http://localhost:8080`, o
gateway do `infra/docker-compose.yml`).

## Estrutura

```
src/
├── api/         # ApiClient (JWT, JSON, ApiError)
├── app/         # raiz de composicao (createServices) + ServicesProvider
├── auth/        # AuthService + TokenStore
├── design/      # design tokens (cores, espacamento, raios, tipografia)
├── features/    # telas e clientes por dominio (auth, agenda, finance, notes)
├── hooks/       # hooks de dados (TanStack Query) e de auth
├── navigation/  # abas planas + gate de autenticacao (Zustand)
├── store/       # estado global de sessao (Zustand)
├── testing/     # fakes de HTTP e harness de testes com providers
└── ui/          # design system (Button, Input, Card, Chip, EmptyState)
```

Backlog do frontend: login social, React Navigation no alvo nativo, NativeWind,
mais componentes (BottomSheet, Toast, Skeleton), Detox (E2E).
