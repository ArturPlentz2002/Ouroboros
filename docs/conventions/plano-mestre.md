# 📐 PLANO MESTRE — Projeto "Ouroboros"

> Documento de referencia do produto, arquitetura e roadmap. (Codinome original do
> rascunho: "Kairos"; o projeto/repo se chama **Ouroboros** e os pacotes Java usam
> `com.ouroboros.*`.)
>
> App multifuncao: **agenda + controle de custos + notas + arquivos**, backend Java
> em microsservicos, frontend React Native, mensageria, OAuth2/JWT, Docker e CI/CD.
> Cada fase tem **objetivo, entregaveis, tarefas e criterio de pronto**. Execute em
> ordem, **uma task (DDE) por branch/PR** (ver Secao 15).

---

## 0. ⚠️ LEIA PRIMEIRO — Recomendacao pragmatica

Microsservicos resolvem problemas de escala organizacional e de carga. A estrategia
de referencia (monolito modular -> extrair) esta descrita abaixo; **neste projeto
optou-se por microsservicos desde o inicio** (ver ADR-0001), com cada modulo de
dominio ja como servico botavel. Kafka, RabbitMQ, Mongo, Cassandra e S3 entram
**quando houver caso de uso real** — o roadmap diz quando.

---

## 1. 🎯 Visao de produto

### Conceito
Um **organizador pessoal multifuncao** que une numa so base:
- **Agenda/Calendario**: eventos, lembretes, recorrencia.
- **Financas**: gastos/receitas, categorias, vincular gastos a eventos da agenda.
- **Notas**: anotacoes livres, checklists, tags.
- **Arquivos**: anexar documentos/comprovantes a eventos, notas ou lancamentos.

O diferencial e a **integracao**: um evento pode ter custo + nota + anexo.

### MVP (corte tudo o resto)
- Cadastro/login (e-mail+senha **e** login social).
- CRUD de eventos da agenda.
- CRUD de lancamentos financeiros + categorias.
- Tela de resumo: "quanto gastei este mes".
- App React Native consumindo a API, **bonito** (ver Secao 12).

### Backlog de produto
Notas, recorrencia, lembretes/push, upload de arquivos (S3), relatorios/graficos,
compartilhamento, exportacao CSV/PDF, offline-first.

### Dica de dominio
Agregados que se relacionam **por ID** (sem join cross-banco):
```
User  ──1:N──>  AgendaEvent
User  ──1:N──>  FinanceEntry  ──N:1──> Category
AgendaEvent  <──opcional──>  FinanceEntry   (vinculo por eventId)
```

---

## 2. 🏗️ Arquitetura-alvo

```
RN App ──HTTPS/JWT──> API Gateway (Spring Cloud Gateway)
  Gateway -> Auth (OAuth2+JWT+Social, Postgres)
  Gateway -> User (Postgres)
  Gateway -> Agenda (Postgres)
  Gateway -> Finance (Postgres)
  Gateway -> Notes (MongoDB)
  Gateway -> Files (S3 + metadados Postgres)
  Gateway -> Notification
  Gateway -> Analytics (Cassandra)
  Agenda/Finance --eventos--> Kafka --> Analytics, Notification
  Agenda --tarefas--> RabbitMQ --> Notification
```

### Banco por servico
| Servico | Banco | Justificativa |
|---|---|---|
| Auth, User, Agenda, Finance | PostgreSQL | Relacional, transacional. |
| Notes | MongoDB | Documentos flexiveis. |
| Analytics | Cassandra | Alta escrita, serie temporal. |
| Files | S3 (+ metadados Postgres) | Binarios fora do banco relacional. |

### Kafka vs RabbitMQ
- **Kafka** -> event streaming (pub/sub duravel, replay, analytics).
- **RabbitMQ** -> work queues (tarefas: e-mail, push, relatorios).

---

## 3. 🧰 Stack

**Backend:** Java 21, Spring Boot 3.3+, Spring Web/Data JPA/Security (Resource
Server OAuth2), Spring Cloud Gateway/Config/Eureka (fases finais), Spring Kafka,
Spring AMQP, Spring Data MongoDB/Cassandra (fases finais), Flyway, MapStruct,
Lombok, JUnit 5, Mockito, Testcontainers, RestAssured/MockMvc, Maven multi-modulo.

**Frontend:** React Native CLI puro (bare), TypeScript, React Navigation, TanStack
Query, Axios, Zustand, react-native-keychain, NativeWind ou Tamagui, Reanimated,
Jest + RNTL, Detox.

**Infra:** Docker + Compose; Postgres/Mongo/Cassandra/Kafka(KRaft)/RabbitMQ; GitHub
Actions; AWS S3 (LocalStack em dev).

---

## 4. 🗂️ Estrutura de repositorios (monorepo)

```
ouroboros/
├── backend/
│   ├── pom.xml                  # parent multi-modulo
│   ├── gateway/
│   ├── service-auth/
│   ├── service-user/
│   ├── service-agenda/
│   ├── service-finance/
│   ├── service-notes/           # fase 6
│   ├── service-files/           # fase 7
│   ├── service-notification/    # fase 5
│   ├── service-analytics/       # fase 8
│   └── shared/
├── mobile/                      # React Native
├── infra/
│   ├── docker-compose.yml
│   ├── docker-compose.deps.yml
│   └── localstack/
├── .github/workflows/
├── docs/
│   ├── adr/
│   └── conventions/             # este doc + convencoes
├── .gitignore
└── README.md
```

### Estrutura interna de um servico (hexagonal simplificado)
```
service-finance/src/main/java/com/ouroboros/finance/
├── domain/        # entidades, regras, VOs (sem framework)
├── application/   # casos de uso
├── adapter/in/web/        # controllers
├── adapter/out/persistence/
├── adapter/out/messaging/
├── config/        # security, cors, beans
└── FinanceApplication.java
```

---

## 5. 🗺️ ROADMAP EM FASES

- **FASE 0** — Fundacoes do repositorio (padroes, CI, protecao de branch).
- **FASE 1** — Esqueleto: Auth(JWT) + Agenda CRUD + Postgres + Docker + CORS + testes.
- **FASE 2** — Login social (OAuth2) + User Service.
- **FASE 3** — Financas + resumo mensal + vinculo com agenda.
- **FASE 4** — CI/CD + semantic-release no GitHub.
- **FASE 5** — Mensageria (Kafka + RabbitMQ) + notificacoes.
- **FASE 6** — Notas (MongoDB).
- **FASE 7** — Arquivos (S3/LocalStack).
- **FASE 8** — Analytics (Cassandra).
- **FASE 9** — Observabilidade + deploy.
- **FASE F (transversal)** — Frontend RN com design system (ver Secao 12).

---

## 6. 🔐 Seguranca: OAuth2 + JWT + login social

**E-mail/senha:** `POST /auth/login` -> BCrypt -> access token (~15min) + refresh
token rotacionado. App guarda no Keychain/Keystore. Header `Authorization: Bearer`.

**Social:** App pega ID token do provedor -> `POST /auth/social {provider, idToken}`
-> backend valida assinatura via JWKS, `iss`, `aud`, `exp` -> cria/associa usuario
-> emite JWT do Ouroboros.

**Validacao:** cada servico e Resource Server validando o JWT localmente pela chave
publica (RS256, via JWKS do auth).

**CORS:** liberar origem em dev (`http://localhost:*`) e prod.

---

## 7. 📨 Mensageria — topicos e filas

**Kafka:** `finance.entry.created|updated|deleted`, `agenda.event.created`,
`agenda.event.reminder.due`, `user.registered`.
**RabbitMQ:** `notifications.email`, `notifications.push`, `reports.generate`.
Padroes: Outbox, idempotencia no consumidor, DLQ.

---

## 8. 🧪 Testes & TDD (JUnit)

**Ciclo:** Red -> Green -> Refactor (teste falha antes do codigo).
**Piramide:** Unitarios (JUnit 5 + Mockito) -> Integracao (`@SpringBootTest` +
Testcontainers) -> API (MockMvc/RestAssured) -> E2E (poucos, via Compose).
**Frontend:** Jest + RNTL (unit/componente); Detox (E2E).
**Gate:** cobertura minima via JaCoCo no CI.

---

## 9. 🐳 Docker — `docker-compose.deps.yml`

Postgres (16), RabbitMQ (3-management), Kafka (KRaft), Mongo (7, fase 6),
LocalStack (S3, fase 7). Dockerfile multi-stage por servico (maven build -> jre).

---

## 10. 🔄 CI — `.github/workflows/ci.yml`

`mvn -B clean verify` + `mvn jacoco:report` em PR/push, Java 21 (temurin), cache
maven.

---

## 11. 📱 Frontend React Native — estrutura

```
mobile/src/
├── api/         # axios + interceptors (JWT, refresh no 401)
├── auth/        # login social, storage seguro (keychain)
├── theme/       # design tokens
├── ui/          # design system
├── features/    # agenda/ finance/ notes/
├── navigation/  # React Navigation (stack + tabs)
├── store/       # Zustand
└── hooks/
```

---

## 12. 🎨 Design System & estilizacao

Tokens (cores indigo `#4F46E5` etc., tipografia Inter, espacamento base 4, raio 8),
dark mode, animacoes sutis (Reanimated), acessibilidade (toque >= 44px, contraste
AA). Biblioteca: NativeWind (recomendado) ou Tamagui. Componentes-base: Button,
Input, Card, Chip, BottomSheet, EmptyState, Skeleton, Toast.

---

## 13. 🌿 Git: convencoes (resumo)

Detalhe completo em [`../git-conventions.md`](../git-conventions.md).

- **Branch:** `DDE-<num>-<tipo>-<slug-kebab>`.
- **Commit/PR:** `<tipo>(DDE-<num>): <descricao imperativa>` (Conventional Commits;
  ticket no escopo p/ o semantic-release).
- **Versao:** `fix:`->PATCH, `feat:`->MINOR, `feat!`/`BREAKING CHANGE:`->MAJOR.
- **Sem assinatura de IA** em commits ou PRs.

---

## 16. ✅ BACKLOG DE TASKS (IDs DDE)

### FASE 0 — Fundacoes
DDE-001 (.gitignore) · DDE-002 (parent pom) · DDE-003 (ADR + plano) ·
DDE-004 (ci.yml) · DDE-005 (protecao de branch) · DDE-006 (CODEOWNERS) ·
DDE-007 (commitlint+husky) · DDE-008 (titulo de PR) · DDE-009 (editorconfig+spotless) ·
DDE-010 (README).

### FASE 1 — Esqueleto
DDE-020 (compose deps/Postgres) · DDE-021 (auth register/BCrypt) ·
DDE-022 (testes auth) · DDE-023 (login+JWT RS256+refresh) ·
DDE-024 (agenda CRUD protegido) · DDE-025 (integracao Testcontainers) ·
DDE-026 (gateway) · DDE-027 (CORS) · DDE-028 (Flyway) · DDE-029 (Dockerfiles+compose).

### Fases 2-9 e F
Login social/User · Financas · CI/CD+release · Mensageria · Notas/Mongo ·
Arquivos/S3 · Analytics/Cassandra · Observabilidade/deploy · Frontend RN.

---

## 17. ⚖️ Resumo das decisoes

| Decisao | Escolha |
|---|---|
| Arquitetura | Microsservicos desde o inicio (ver ADR-0001) |
| Auth | JWT RS256 + refresh + social; validacao offline via JWKS |
| Mensageria | Kafka (eventos) + Rabbit (tarefas) — fase 5 |
| Bancos | Postgres core, Mongo notas, Cassandra analytics |
| Release | semantic-release + DDE no escopo |
| Branch flow | 1 task DDE = 1 branch (de develop) = 1 PR |
| Frontend | RN + DS (NativeWind) + tokens |
