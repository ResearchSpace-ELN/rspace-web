# RSpace Code-Review Subagents

Project-local, committed code-review agent definitions tuned to **rspace-web**
conventions. They live here (under `.agents/`, alongside `.agents/skills/`) so
they are shared with the whole team via git and readable by any AGENTS.md-aware
agent (Claude Code, Cursor, Codex CLI, Gemini CLI). No install step and no
symlink are required to use them in this repo - point your agent at the file.

## The agents

| File | Reviews | Key rules enforced |
|------|---------|--------------------|
| `rspace-layering-reviewer.md` | Backend architecture | Controller -> Service(Manager) -> DAO direction, no upward imports, `*Manager` naming for AOP transactions, transaction boundaries |
| `rspace-i18n-reviewer.md` | i18n externalization | All user-facing strings resolved from message bundles, not literals; resolved via injected `MessageSource` |
| `rspace-security-reviewer.md` | Security model | Shiro permission checks (`DOMAIN:ACTION:IDENTIFIER`), API auth, no secrets in logs, DOMPurify for user HTML |
| `rspace-persistence-reviewer.md` | DB & migrations | Liquibase changeset format, Envers auditing, soft deletes, lazy-loading pitfalls |
| `rspace-frontend-reviewer.md` | React/TypeScript | Functional components, React Query for new state, centralized Axios, DOMPurify, test harness conventions, no new deps |

Each agent reviews the unstaged `git diff` by default. They are read-only:
they report findings (Critical / Important / Suggestions / Strengths with
`file:line`), they do not edit code, and they do not run tests, `tsc`, or lint.

## How to invoke

Point your agent at the relevant file(s). Examples:

- "Review my staged backend changes using `.agents/agents/rspace-layering-reviewer.md`."
- "Run the i18n and security reviewers in `.agents/agents/` over this diff."

## Relationship to the `pr-review-toolkit` plugin

These complement, they do not replace, the official `pr-review-toolkit` plugin.

- **`pr-review-toolkit`** covers generic, language-agnostic dimensions:
  general bugs/quality, silent failures, type design, test coverage, comment
  accuracy, and simplification. Once installed it provides spawnable subagents.
- **These rspace agents** cover the domain rules the generic suite does not
  know: RSpace's layering, i18n externalization, Shiro security model, Liquibase
  / Envers persistence, and the frontend React Query / DOMPurify conventions.

A thorough review runs both: the toolkit for generic quality, these for
rspace-specific conventions.

## Optional: spawnable subagents in Claude Code

Claude Code natively auto-discovers subagents from `.claude/agents/`, which is
**gitignored** in this repo, so these are intentionally kept in `.agents/`
instead and used by pointing Claude at the file (the same pattern this repo uses
for `.agents/skills/`). A Claude Code user who wants them to appear as
auto-spawnable `subagent_type`s can optionally create a local, gitignored
symlink:

```
ln -snf ../.agents/agents .claude/agents
```

This is per-user and entirely optional; it is not required to use the agents.
