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

> Disponivel ao final da Fase 1 (DDE-020 e DDE-029). Resumo do fluxo previsto:
>
> ```bash
> # dependencias (Postgres) para desenvolvimento local
> docker compose -f infra/docker-compose.deps.yml up -d
>
> # stack completa (postgres + auth + agenda + gateway)
> docker compose -f infra/docker-compose.yml up --build
> ```
>
> Fluxo de autenticacao (via gateway em :8080): `POST /auth/register` ->
> `POST /auth/login` (recebe access+refresh) -> chamadas a `/api/v1/agenda/**`
> com `Authorization: Bearer <access>` -> `POST /auth/refresh` quando expirar.

## Convencoes de contribuicao

- **1 task (DDE) = 1 branch = 1 PR** pequeno e focado, a partir de `develop`.
- **Branch:** `DDE-<num>-<tipo>-<slug-kebab>`.
- **Commit/PR:** `<tipo>(DDE-<num>): <descricao imperativa>` (Conventional Commits).
- **TDD:** escreva o teste que falha antes do codigo.

Detalhes em [`docs/git-conventions.md`](docs/git-conventions.md) e nas instrucoes do
agente em [`CLAUDE.md`](CLAUDE.md).
