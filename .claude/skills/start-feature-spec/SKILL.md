---
name: start-feature-spec
description: Bootstrap a new spec-driven feature from the main branch - creates the feature/<x>/base integration branch, a feature/<x>/spec branch, generates the spec/ folder via the spec-workflow skill, and opens the plan-approval PR. Use when the user is on main and asks to "start a new feature", "create a new spec", "start a spec branch", or wants to kick off a large feature with a spec folder.
---

# start-feature-spec — bootstrap a spec-driven feature from main

Turns "I want to start feature X" into the branch structure and plan-approval PR that
the spec-driven workflow needs. The end state:

```
main
 └── feature/<x>/base   (integration branch, pushed — task branches will target it)
      └── feature/<x>/spec   (pushed, PR open into feature/<x>/base with the spec/ folder)
```

Merging that PR is the team's approval of the plan; task branches
(`task/<n>-<short-name>`, per `spec/AGENT.md`) are then cut from `feature/<x>/base`.

## Preconditions — verify before doing anything

1. The local repository is on `main`: `git branch --show-current`.
2. The working tree is clean (`git status --porcelain` is empty) and `main` is up to
   date (`git pull --ff-only`).

If either fails, stop and tell the developer what to resolve — do not stash or commit
on their behalf.

## Steps

### 1. Ask for the feature name and description

Ask the developer for:
- a **short feature reference** `<x>` — normalize to kebab-case, lowercase, no slashes
  (e.g. "Kotlin sync server" → `kotlin-sync-server`); confirm the normalized name, and
- a **one/two-sentence description** of the feature — this seeds the spec exploration
  and the PR body.

### 2. Create and push the integration branch

```bash
git checkout -b feature/<x>/base
git push -u origin feature/<x>/base
```

No commits are needed on it — it marks the integration point task PRs will target.

### 3. Create the spec branch

```bash
git checkout -b feature/<x>/spec
```

### 4. Generate the spec/ folder

Run the **spec-workflow** skill (`.claude/skills/spec-workflow/SKILL.md`) with the
feature description as input. That skill owns the whole spec authoring process:
codebase exploration, decision questions to the developer, task split, `spec/AGENT.md`,
`spec/README.md`, `TASK_N_SPEC.md` files, and atomic commits.

Two parameters to carry into it:
- The **integration branch** named throughout the generated `spec/AGENT.md` and
  `spec/README.md` is `feature/<x>/base` (task branches cut from it and PR back into
  it).
- Its atomic commits land here on `feature/<x>/spec` (not directly on the integration
  branch).

### 5. Push the spec branch

After the spec/ folder is complete and committed:

```bash
git push -u origin feature/<x>/spec
```

### 6. Open the plan-approval PR

Start a sub-agent, to act as a senior Kotlin developer, to review the changes made on `feature/<x>/spec` against `feature/<x>/base`, get a feedback and update/fix wherever necessary.

This is done to ensure that a higher quality PR is created at the end.

### 7. Open the plan-approval PR

Create a PR from `feature/<x>/spec` into `feature/<x>/base`:

```bash
gh pr create --base feature/<x>/base --head feature/<x>/spec \
  --title "Spec: <feature name>" \
  --body "<see template below>"
```

PR body template:

```markdown
## Plan approval: <feature name>

<one-paragraph feature description>

This PR adds the spec-driven task breakdown for the feature (see `spec/README.md` for
the architecture, decisions record, and task table). Merging it approves the plan;
implementation then proceeds task-by-task on `task/<n>-<short-name>` branches
targeting `feature/<x>/base`, following `spec/AGENT.md`.

### Tasks
<copy of the task table from spec/README.md>
```

Report the PR URL to the developer.

## Notes

- The `clean-spec-guard` workflow only blocks `spec/` from PRs targeting `main`, so the
  spec PR into `feature/<x>/base` passes; the folder must be removed before
  `feature/<x>/base` is finally merged into `main`.
- The `spec-progress-guard` workflow applies to every PR, including this one: never
  commit `spec/PROGRESS.md`.
