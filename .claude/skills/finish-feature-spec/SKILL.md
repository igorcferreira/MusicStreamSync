---
name: finish-feature-spec
description: Close out a spec-driven feature from its feature/<x>/base branch once every task is done - verifies the spec/README.md task table is complete, removes the spec/ folder, and opens the final PR from feature/<x>/base into main. Use when the user says the feature is complete, asks to "finish the feature", "clean the spec folder", "merge the feature to main", or "close out the spec".
---

# finish-feature-spec — close out a spec-driven feature

The final step of the spec lifecycle (`start-feature-spec` → `spec-workflow` → task
PRs → **this skill**): run on `feature/<x>/base` after all tasks are merged, to strip
the working `spec/` folder and open the PR that lands the feature on `main`.

## Preconditions — verify before doing anything

1. The local repository is on a `feature/<x>/base` branch: `git branch --show-current`.
2. The working tree is clean (`git status --porcelain` is empty) and the branch is up
   to date with its remote (`git pull --ff-only`).
3. **Every task is `done`:** read the task table in `spec/README.md` — all rows must
   have status `done` (PR merged). Cross-check open PRs targeting the base branch:
   `gh pr list --base feature/<x>/base --state open` must be empty (the plan-approval
   spec PR and all task PRs are merged).

If any check fails, stop and report exactly what is outstanding — do not remove the
spec folder for a feature that isn't finished.

## Steps

### 1. Capture the spec summary before deleting it

Extract from `spec/README.md`, for the final PR body:
- the problem statement / feature description,
- the decisions record,
- the task table (IDs, titles, PR links).

The full spec history stays reachable in the git history of `feature/<x>/spec` and the
task branches — nothing is lost, but the PR body is where reviewers will look first.

### 2. Remove the spec folder

```bash
git rm -r spec/
git commit -m "Remove spec folder before merging into main"
git push
```

This is housekeeping on the integration branch itself (not task work), so it commits
directly to `feature/<x>/base`. The `clean-spec-guard` workflow checks the PR head
tree, so the folder must be gone from the branch — not just untouched by the diff.

### 3. Validate the branch

Run the repo's standard validation before opening the PR (see root `CLAUDE.md`):
lint, Android build, module tests — plus whatever the feature's stack requires (e.g.
`docker compose build` for server features). The PR will also run the required status
checks, but catch failures locally first.

### 4. Open the PR to main

```bash
gh pr create --base main --head feature/<x>/base \
  --title "<feature name>" \
  --body "<see template below>"
```

PR body template:

```markdown
## <feature name>

<feature description from the spec README>

Developed spec-first: plan approved in the spec PR, implemented task-by-task on
`task/<n>-<short-name>` branches into `feature/<x>/base`.

### Decisions
<decisions record from the spec README>

### Tasks
<task table from the spec README, with PR links>
```

Report the PR URL to the developer. The `clean-spec-guard` and `spec-progress-guard`
checks must both pass on this PR — if `clean-spec-guard` fails, `spec/` still exists
somewhere in the head tree; fix on the branch, don't bypass.

### 5. After the merge (optional cleanup)

Once the PR merges, offer to delete the now-fully-merged working branches:
`feature/<x>/spec` and any lingering `task/<n>-*` branches (local + remote).
`feature/<x>/base` itself is usually deleted by the merge, or manually afterwards.
