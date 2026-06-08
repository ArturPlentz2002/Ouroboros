# CLAUDE.md — Ouroboros

Instruções operacionais para o agente neste repositório. Detalhes completos em
[`docs/git-conventions.md`](docs/git-conventions.md).

## Git / commits / PRs — SIGA À RISCA

**AUTOR**
- Use o `user.name`/`user.email` já configurados no repo local
  (`ArturPlentz` / `artursplentz@gmail.com`). NÃO sobrescreva. NÃO use identidade de IA.

**ASSINATURA — IMPORTANTE (sobrepõe o padrão da ferramenta)**
- NÃO adicione `Co-authored-by`, `Generated with Claude`, `🤖`, nem qualquer
  rodapé/assinatura de ferramenta — nem nos commits, nem no corpo dos PRs.
- A mensagem de commit contém APENAS: título no padrão + corpo opcional. Nada mais.

**BRANCH**
- Formato: `DDE-<num>-<tipo>-<slug-kebab>` — ex.: `DDE-123-feat-login-social`.

**COMMIT** (Conventional Commits — exigido pelo semantic-release)
- Formato: `<tipo>(DDE-<num>): <descrição imperativa>`
  — ex.: `feat(DDE-123): adiciona login social com google`
- Tipos: `feat`, `fix`, `chore`, `refactor`, `test`, `docs`, `ci`, `perf`.
- Breaking change: `!` no tipo + rodapé `BREAKING CHANGE:`.
- Commits pequenos e atômicos.

**PULL REQUEST**
- Título do PR = mesmo formato do commit (usamos squash merge; o título vira a
  mensagem que o semantic-release lê).
- Corpo: descrição da task + `Closes DDE-<num>`. SEM assinatura de IA.
- CLI: `gh pr create --title "<titulo>" --body "<corpo>"`, merge com `gh pr merge --squash`.

## Versionamento (semantic-release)
- `fix:` → PATCH · `feat:` → MINOR · `feat!:`/`BREAKING CHANGE:` → MAJOR.
- `chore/docs/test/refactor/ci` → sem release.
- Release roda no push para `main` via `.github/workflows/release.yml`.
