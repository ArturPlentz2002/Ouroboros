# Ouroboros

Organizador pessoal multifuncao (**agenda + custos + notas + arquivos**) construido
como exercicio de arquitetura de microsservicos. Backend Java/Spring Boot, frontend
React Native (fase posterior), Postgres, mensageria e CI/CD.

> Plano completo do produto/roadmap: [`docs/conventions/plano-mestre.md`](docs/conventions/plano-mestre.md).
> Decisao de stack/arquitetura: [`docs/adr/0001-stack.md`](docs/adr/0001-stack.md).

## Arquitetura (alvo)

Microsservicos atras de um API Gateway, cada servico dono dos seus dados (integracao
por ID, sem join cross-banco). Autenticacao com **JWT RS256**: o `service-auth`
emite os tokens e expoe um **JWKS**; os demais servicos validam localmente como
Resource Servers.

| Servico | Porta | Responsabilidade | Banco |
|---|---|---|---|
| gateway | 8080 | Roteamento, CORS, validacao JWT de borda | — |
| service-auth | 8081 | Registro, login, refresh, emissao de JWT, JWKS | Postgres (`auth`) |
| service-agenda | 8082 | CRUD de eventos da agenda | Postgres (`agenda`) |

> service-auth, service-agenda e gateway entram na Fase 1. Hoje o repositorio ja tem
> as fundacoes (Fase 0): parent Maven, modulo `shared`, CI, lint e convencoes.

## Estrutura do repositorio

```
backend/      # projeto Maven multi-modulo
  pom.xml     # parent (Spring Boot 3.3, Spring Cloud 2023, Spotless, JaCoCo)
  shared/     # biblioteca comum
infra/        # docker-compose e dependencias locais (Fase 1+)
docs/
  adr/        # Architecture Decision Records
  conventions/# plano mestre e convencoes
.github/workflows/  # ci.yml, release.yml, pr-title.yml
```

## Pre-requisitos

- **Java 21** (Temurin/OpenJDK)
- **Maven 3.8+**
- **Docker** + **Docker Compose** (para dependencias e para subir os servicos)
- **Node 20+** (apenas para o tooling de commit: commitlint/husky)

## Como rodar

### 1. Build e testes do backend

```bash
# build completo (Spotless + testes + JaCoCo)
mvn -f backend/pom.xml clean verify

# so compilar/instalar (sem testes)
mvn -f backend/pom.xml clean install -DskipTests

# formatar o codigo conforme o padrao (google-java-format)
mvn -f backend/pom.xml spotless:apply
```

### 2. Ativar os hooks de commit (uma vez)

```bash
npm install   # instala husky + commitlint e ativa o hook commit-msg
```

### 3. Subir a aplicacao (Docker)

```bash
# apenas as dependencias (Postgres) para rodar os servicos pela IDE
docker compose -f infra/docker-compose.deps.yml up -d

# stack completa (postgres + service-auth + service-agenda + gateway)
docker compose -f infra/docker-compose.yml up --build
```

Apenas o **gateway** expoe porta no host (`8080`); os servicos conversam pela rede
interna. Se a porta 8080 estiver ocupada, defina `GATEWAY_HOST_PORT` (e
`POSTGRES_HOST_PORT` para o compose de dependencias).

### 4. Fluxo de autenticacao (via gateway em `:8080`)

```bash
# 1. registrar
curl -X POST localhost:8080/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"voce@ouroboros.dev","password":"password1"}'

# 2. logar -> recebe accessToken + refreshToken
curl -X POST localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"voce@ouroboros.dev","password":"password1"}'

# 3. criar evento (autenticado)
curl -X POST localhost:8080/api/v1/agenda/events \
  -H "Authorization: Bearer <accessToken>" \
  -H 'Content-Type: application/json' \
  -d '{"title":"Jantar","startsAt":"2026-07-01T20:00:00Z"}'

# 4. renovar tokens quando o access expirar
curl -X POST localhost:8080/auth/refresh \
  -H 'Content-Type: application/json' \
  -d '{"refreshToken":"<refreshToken>"}'
```

## Convencoes de contribuicao

- **1 task (DDE) = 1 branch = 1 PR** pequeno e focado, a partir de `develop`.
- **Branch:** `DDE-<num>-<tipo>-<slug-kebab>`.
- **Commit/PR:** `<tipo>(DDE-<num>): <descricao imperativa>` (Conventional Commits).
- **TDD:** escreva o teste que falha antes do codigo.

Detalhes em [`docs/git-conventions.md`](docs/git-conventions.md) e nas instrucoes do
agente em [`CLAUDE.md`](CLAUDE.md).
