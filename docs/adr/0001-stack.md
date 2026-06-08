# ADR-0001 — Escolha de stack e arquitetura inicial

- **Status:** Aceito
- **Data:** 2026-06-08
- **Contexto do projeto:** Ouroboros — organizador pessoal multifuncao (agenda + financas + notas + arquivos).

## Contexto

Precisamos de uma base que comporte crescimento em dominios independentes (auth,
agenda, financas, notas, arquivos, analytics) e que sirva como exercicio de
arquitetura distribuida moderna. As escolhas precisam equilibrar produtividade
inicial, testabilidade e um caminho claro de evolucao.

## Decisao

**Arquitetura: microsservicos desde o inicio.** A Fase 1 entrega tres servicos
botaveis independentes — `gateway`, `service-auth`, `service-agenda` — mais uma
biblioteca comum `shared` e um parent Maven multi-modulo. Cada servico e dono dos
seus dados; integracoes entre dominios acontecem por ID, nunca por join
cross-banco.

**Backend:**
- Java 21 (LTS), Spring Boot 3.3.x, Spring Cloud 2023.0.x.
- Spring Web, Spring Data JPA, Spring Security (Resource Server OAuth2).
- Spring Cloud Gateway para roteamento/CORS de borda.
- Flyway para migrations, MapStruct para mapeamento, Lombok.
- JUnit 5, Mockito, Testcontainers, MockMvc/RestAssured.
- Maven multi-modulo (parent em `backend/pom.xml`).

**Seguranca:** JWT assinado em **RS256**. O `service-auth` emite os tokens e expoe
um endpoint **JWKS**; os demais servicos validam localmente como Resource Servers
via `jwk-set-uri` (sem chamar o auth a cada request). Refresh token rotacionado e
persistido. Senhas com BCrypt.

**Dados:** PostgreSQL para os dominios core (um banco por servico). MongoDB
(notas), Cassandra (analytics) e S3 (arquivos) entram em fases posteriores, quando
houver caso de uso real.

**Mensageria (fase posterior):** Kafka para eventos de dominio (pub/sub duravel) e
RabbitMQ para filas de trabalho (e-mail/push/relatorios).

**Infra/Dev:** Docker + Docker Compose; CI no GitHub Actions; semantic-release para
versionamento automatico; LocalStack para S3 em dev.

## Consequencias

**Positivas:**
- Fronteiras de dominio explicitas desde o dia 1; cada servico escala/deploya
  isolado.
- Validacao de JWT offline nos servicos (RS256 + JWKS) reduz acoplamento ao auth.
- Persistencia poliglota aplicada so onde agrega valor.

**Negativas / custos aceitos:**
- Mais setup e mais containers do que um monolito; CI mais lento.
- Complexidade operacional (varios servicos, varios bancos) exige disciplina de
  testes e observabilidade.
- Consistencia entre dominios passa a ser eventual (resolvida com eventos/Outbox
  nas fases de mensageria).

## Alternativas consideradas

- **Monolito modular primeiro, extrair depois:** mais produtivo no comeco, menos
  overhead. Preterido porque o objetivo inclui praticar arquitetura de
  microsservicos desde ja.
