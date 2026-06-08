# 🔧 Convenções de Git, Semantic-Release e Instruções para o Agente

> Referência viva do projeto **Ouroboros**. As instruções operacionais para o
> agente estão resumidas no `CLAUDE.md` da raiz (carregado automaticamente).

---

## 1. Identidade dos commits

A identidade já está fixada **neste repositório** (`git config` local):

```bash
git config user.name "ArturPlentz"
git config user.email "artursplentz@gmail.com"
```

Para privacidade, dá para trocar pelo e-mail `noreply` do GitHub
(**Settings → Emails → "Keep my email private"**), no formato
`NNNNNN+ArturPlentz2002@users.noreply.github.com`.

> A "assinatura do Claude" (`Co-authored-by:` / `Generated with Claude`) **não**
> vem do Git — é o agente que adiciona no corpo do commit. Neste repo ela está
> **desativada** por instrução no `CLAUDE.md`. Não existe config de Git para isso.

---

## 2. Padrão de nomenclatura

### 2.1 Branch (estilo DDE)
```
DDE-<num>-<tipo>-<slug-da-task>
```
- `<num>`: número do ticket (muda a cada task)
- `<tipo>`: `feat`, `fix`, `chore`, `refactor`, `test`, `docs`, `ci`, `perf`
- `<slug>`: kebab-case curto

**Exemplos:**
```
DDE-123-feat-login-social
DDE-145-fix-refresh-token-expirado
DDE-160-chore-config-docker-compose
```

### 2.2 Commit e título do PR (Conventional Commits — exigido pelo semantic-release)
```
<tipo>(DDE-<num>): <descrição no imperativo>
```
O ticket `DDE-000` entra no **escopo** (entre parênteses). É assim que mantemos o
padrão DDE **e** o semantic-release funcionando.

**Exemplos:**
```
feat(DDE-123): adiciona login social com google
fix(DDE-145): corrige refresh de token expirado
chore(DDE-160): configura docker-compose das dependencias
```

Breaking change (sobe versão MAJOR) — use `!` ou rodapé:
```
feat(DDE-200)!: altera contrato do endpoint de auth

BREAKING CHANGE: /auth/login agora exige campo deviceId
```

### 2.3 Como cada tipo afeta a versão (semantic-release)
| Tipo de commit | Efeito na versão |
|---|---|
| `fix:` | PATCH (1.2.**3** → 1.2.**4**) |
| `feat:` | MINOR (1.**2**.0 → 1.**3**.0) |
| `feat!:` ou `BREAKING CHANGE:` | MAJOR (**1**.0.0 → **2**.0.0) |
| `chore:`, `docs:`, `test:`, `refactor:`, `ci:` | nenhum release (padrão) |

---

## 3. Configuração do semantic-release

`.releaserc.json` na raiz (já criado). O plugin `@semantic-release/exec` roda
`mvn versions:set` para que a versão do(s) `pom.xml` acompanhe a tag — útil
porque o backend é Maven e o semantic-release é Node.

> ⚠️ O passo `mvn versions:set` exige que exista pelo menos um `pom.xml` na raiz
> quando o release rodar. Enquanto o projeto Maven não for criado, o release no
> CI vai falhar nesse passo — crie o `pom.xml` antes do primeiro push relevante
> em `main`, ou comente temporariamente o bloco `@semantic-release/exec`.

---

## 4. Workflow de release

`.github/workflows/release.yml` (já criado) roda **apenas no push para `main`**.
Combine com um `ci.yml` (build + testes em PR). O `release.yml` instala o
semantic-release e executa `npx semantic-release`.

---

## 5. Instruções para o AGENTE

> O bloco abaixo está replicado de forma operacional no `CLAUDE.md` da raiz, que
> o Claude Code carrega automaticamente em toda sessão neste repositório.

```
AUTOR
- Use o user.name e user.email já configurados no repositório local.
- NÃO sobrescreva a config de autor. NÃO use nome/e-mail de IA.

ASSINATURA
- NÃO adicione "Co-authored-by", "Generated with Claude", "🤖", nem qualquer
  rodapé/assinatura de ferramenta nos commits OU no corpo dos PRs.
- A mensagem do commit deve conter APENAS: linha de título no padrão + corpo
  opcional. Nada mais.

BRANCH
- Crie a branch no formato: DDE-<num>-<tipo>-<slug-kebab>
  Ex.: DDE-123-feat-login-social

COMMIT (Conventional Commits, exigido pelo semantic-release)
- Formato: <tipo>(DDE-<num>): <descrição imperativa>
  Ex.: feat(DDE-123): adiciona login social com google
- Tipos: feat, fix, chore, refactor, test, docs, ci, perf.
- Breaking change: use "!" no tipo e rodapé "BREAKING CHANGE:".
- Commits pequenos e atômicos.

PULL REQUEST
- Título do PR = MESMO formato do commit (squash merge: o título vira a
  mensagem final que o semantic-release lê).
  Ex.: feat(DDE-123): adiciona login social com google
- Corpo do PR: descrição da task + "Closes DDE-123". SEM assinatura de IA.
- Use a CLI: gh pr create --title "<titulo>" --body "<corpo>"
```

---

## 6. Comandos de uso (fluxo de uma task)

```bash
# 1. branch
git switch -c DDE-123-feat-login-social

# 2. trabalhar + commitar
git add .
git commit -m "feat(DDE-123): adiciona login social com google"

# 3. enviar
git push -u origin DDE-123-feat-login-social

# 4. abrir PR (sem rodapé de IA)
gh pr create \
  --title "feat(DDE-123): adiciona login social com google" \
  --body "Implementa validação de ID token Google e emissão de JWT.

Closes DDE-123"

# 5. merge com squash (título do PR vira o commit lido pelo semantic-release)
gh pr merge --squash
```

> Configure o repositório para **Allow squash merging** e, idealmente,
> **"Default to PR title"** em Settings → General → Pull Requests.

---

## 7. (Opcional, não ativado) Travar o padrão automaticamente

Para garantir que ninguém — nem o agente — fuja do formato:

- **commitlint** + **husky**: valida a mensagem no `commit-msg` hook.
- **GitHub Action** validando o **título do PR** (ex.: `amannn/action-semantic-pull-request`).

`commitlint.config.js`:
```js
module.exports = {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'scope-empty': [2, 'never'],                 // exige escopo (o DDE)
    'scope-case': [2, 'always', 'upper-case'],   // DDE em maiúsculo
  },
};
```

> Não ativado neste momento (evita adicionar tooling Node a um projeto Maven).
> Quando quiser, peça e eu configuro.

---

## 8. Nota — formato DDE "literal" (avançado, não recomendado)

Se quisesse o commit começando com `DDE-123-feat: ...`, daria para customizar o
parser do semantic-release via `parserOpts.headerPattern` (regex). Mas isso quebra
ferramentas que assumem o padrão Angular, dificulta manutenção e confunde
colaboradores. A abordagem com **ticket no escopo** (`feat(DDE-123): ...`) entrega
o mesmo resultado sem fragilidade. Recomendado ficar com ela.

---

## ⚖️ Sobre autoria
Configurar o Git para commitar no seu nome é normal: o trabalho e a
responsabilidade são seus. Evite apenas usar isso para enganar em contextos onde a
origem do código importa (avaliação acadêmica, teste técnico, política de empresa,
ou open source com regra de disclosure). Em projeto pessoal, sem problema.
