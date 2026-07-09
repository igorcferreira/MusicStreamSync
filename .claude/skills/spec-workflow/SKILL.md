---
name: spec-workflow
description: Create a spec/ folder that splits a large feature into spec-driven, reviewable tasks (AGENT.md + README.md orchestration, TASK_N_SPEC.md per task) developed on per-task branches with PRs. Use when starting a major multi-session feature, when the user asks for a "spec folder", "task breakdown", "spec-driven plan", or to split work so sessions can be reviewed and resumed later.
---

# spec-workflow — spec-driven task breakdown for large features

Recreate the `spec/` folder structure used for the sync-server feature (see git history
of `feature/kotlin_server` for the reference implementation) for any new large feature.

The output is a folder of markdown files that lets independent, resumable sessions
implement the feature task by task, each on its own branch with its own PR.

## Target structure

```
spec/
├── AGENT.md          # session protocol: how an agent works one task
├── README.md         # orchestrator: overview, architecture, decisions, task table
├── TASK_1_SPEC.md    # one spec per task
├── ...
└── TASK_N_SPEC.md
```

`spec/PROGRESS.md` is NOT created up front: it is a branch-local work journal created
while working a task branch and deleted before its PR opens (CI enforces this —
`.github/workflows/spec-progress-guard.yml`). The whole `spec/` folder never merges to
`main` (`.github/workflows/clean-spec-guard.yml`).

## Steps

### 1. Explore before writing

Read the code the feature touches. The specs must encode *codebase facts* (exact file
paths, class/function names, gotchas like missing platform targets or internal-only
APIs) so each task is self-contained for a session that starts cold. Facts you don't
write down will be re-discovered — at full cost — by every future session.

### 2. Settle decisions with the user first

Ask about anything that changes the task split before writing specs: user/data model,
auth flows, persistence, deployment, scope (which platforms now vs later). Record every
decision in a **Decisions record** table in `README.md`.

### 3. Split into tasks

- Each task = one PR-sized, independently reviewable unit with testable acceptance
  criteria. Typical seams: per module, per layer (scaffold → storage/API → core logic →
  wiring → per-platform UI).
- Draw the dependency graph and derive **waves** of tasks that can run in parallel.
- Number tasks `TASK_1..TASK_N` in rough dependency order.

### 4. Write `README.md` (the orchestrator)

Must contain:
- Problem statement, including the user's concrete usage scenario verbatim (it becomes
  acceptance tests in the relevant task spec).
- Architecture diagram (ASCII or mermaid) of the end state.
- Decisions record table.
- **Task table** — the single source of truth for progress:
  `| ID | Title | Depends on | Status | Branch | PR | Spec |`
  with status values `pending / in_progress / in_review (PR open) / done (PR merged)`.
- Dependency graph + parallel waves.
- A `## Session log` section (one line per session appended over time).

### 5. Write `AGENT.md` (the session protocol)

Must contain:
- Workflow: read README table → pick next `pending` task whose deps are `done` → read
  its spec → implement only what it scopes → validate → update table + session log.
- **Branch/PR mandate:** name the integration branch (the feature branch, e.g.
  `feature/<name>` — never commit task work directly to it); each task works on
  `task/<n>-<short-name>` cut from it and ends with a PR back into it.
- **PROGRESS.md protocol:** journal in `spec/PROGRESS.md` while working; delete before
  opening the PR (CI-enforced).
- Project mandates that apply to every task (examples from the sync-server feature:
  single Swagger `openapi.yaml` updated in the same task as any endpoint change;
  Docker-first validation for server tasks). Derive equivalents for the new feature.
- Rules: no scope expansion; specs are the source of truth (update the spec in the task
  branch when it conflicts with reality, note it in the PR).
- A validation-commands table (build, lint, per-module tests) with any environment
  quirks (e.g. this repo builds with JDK 17: `JAVA_HOME=.../temurin-17.jdk/Contents/Home`).

### 6. Write each `TASK_N_SPEC.md`

Fixed skeleton — every section required:

```markdown
# TASK_N — <title>

Branch: `task/<n>-<short-name>` · Depends on: TASK_X, TASK_Y · Protocol: [AGENT.md](AGENT.md)

## Goal
One paragraph: what exists when this task is done, and why.

## Context
Codebase facts a cold session needs: exact file paths, symbols to reuse, constraints
and gotchas discovered in step 1, pointers to contracts owned by other tasks.

## Requirements
Numbered, testable acceptance criteria. Name required tests explicitly.

## Non-goals
What neighboring tasks own — the scope fence.

## Validation
Exact commands (copy from AGENT.md table, plus task-specific smoke checks).
End with: delete `spec/PROGRESS.md`, open PR to the integration branch.
```

### 7. Wire the CI guards

Both workflows already exist in this repo and are feature-agnostic — verify they cover
the new setup, don't duplicate them:
- `.github/workflows/spec-progress-guard.yml` — fails any PR containing
  `spec/PROGRESS.md`.
- `.github/workflows/clean-spec-guard.yml` — fails PRs targeting `main` that carry the
  `spec/` folder.

### 8. Commit

Atomic commits on the integration branch: orchestration files first (`AGENT.md` +
`README.md`), then one commit per task spec, then any workflow/tooling changes.

## Quality bar

- A task spec passes review when an agent given only that file + `AGENT.md` could
  implement it without reading this conversation.
- The dependency table must be acyclic and every task reachable.
- Markdown links must resolve to real repo paths.
