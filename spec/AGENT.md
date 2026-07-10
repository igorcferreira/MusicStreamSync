# AGENT.md — Session protocol for the sync-server spec suite

This folder contains the spec-driven task breakdown for building the MusicStreamSync
**sync server**: a JVM process, built on the existing KMP shared code, that periodically
fetches a user's Apple Music play history, diffs it against their Last.fm history, and
scrobbles the delta. Read [README.md](README.md) first for the architecture, decisions,
and task table.

## Workflow (every session)

1. Read [README.md](README.md): the task table is the single source of truth for status.
2. Pick the next `pending` task whose **Depends on** entries are all `done` (PR merged).
   Tasks with independently-merged dependencies may be worked **in parallel** on separate
   branches.
3. Read that task's `TASK_N_SPEC.md` in full. Implement **only** what the spec scopes.
4. Validate (see [Validation commands](#validation-commands) and the task's own
   Validation section).
5. Update the task's row in `README.md` (status, branch, PR link) and append a one-line
   entry to the Session log at the bottom of `README.md`.

## Branch & PR mandate

- `feature/kotlin-server/base` is the **central integration branch** — the current working
  state. **Never commit task work directly to it.**
- Each task is developed on its own branch cut from `feature/kotlin-server/base`, named
  `task/<n>-<short-name>` (e.g. `task/1-shared-jvm-target`).
- A task ends with a **PR targeting `feature/kotlin-server/base`**.
- Status values in the README table:
  `pending` → `in_progress` → `in_review` (PR open) → `done` (PR merged).
- The README status update and PR link are committed on the task branch and land as part
  of its PR.

## `spec/PROGRESS.md` — per-branch work journal

While working a task branch, keep session progress in `spec/PROGRESS.md`:

- which task is being worked and its branch,
- notes per session (what was done, what was tried and rejected),
- decisions taken (and why) when the spec left room,
- what remains before the task's acceptance criteria pass.

This lets an interrupted session be resumed on that branch by a fresh session. The file
is **branch-local scratch state**:

- It MUST be deleted (deletion committed) **before the PR is opened**.
- The GitHub Actions check `spec-progress-guard` fails any PR whose changes contain
  `spec/PROGRESS.md`.

## API documentation mandate (Swagger/OpenAPI)

Every HTTP API created for consumption by the native apps MUST be documented in the
single Swagger document **`server/openapi.yaml`** (created by TASK_3, served by the
server at `GET /openapi.yaml`).

- Any task that adds or changes an endpoint updates `server/openapi.yaml` **in the same
  task**.
- A task touching the HTTP surface is not `done` until the document matches the
  implemented API (paths, schemas, auth scheme, error responses).

## Docker mandate

All `:server` development, testing, and deployment happens with Docker support:

- The server runs via `docker compose up` (services: `server` + `mongodb`).
- Server tasks must validate **inside the containerized environment**
  (`docker compose build` + a smoke run against the container), not only via the host
  Gradle build.

## Rules

- **Do not expand scope.** If something adjacent needs fixing, note it in the PR
  description or add a follow-up line to the README, don't do it in the task branch.
- **Specs are the source of truth.** If a spec conflicts with what you find in the code,
  update the spec first (in the task branch), note the change in the PR, then implement.
- Follow repo conventions: ktlint/SwiftLint clean, builds green before committing
  (see the root `CLAUDE.md`).
- Commit granularity: at least one commit per task; more is fine.

## Validation commands

All Gradle commands use JDK 17:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
```

| Check | Command |
| --- | --- |
| Kotlin lint | `./gradlew ktlintCheck` |
| Android app builds | `./gradlew :composeApp:assembleDebug` |
| Shared tests (Android host) | `./gradlew :shared:testAndroidHostTest` |
| Shared tests (JVM, after TASK_1) | `./gradlew :shared:jvmTest` |
| Last.fm module tests | `./gradlew :lastfmapi:jvmTest` |
| Server tests (after TASK_3) | `./gradlew :server:test` |
| iOS tests | `./gradlew iosSimulatorArm64Test` |
| iOS app builds (tasks touching `iosApp/`) | `xcodebuild` / xcrun MCP tooling on `iosApp/iosApp.xcodeproj` |
| Server container (tasks touching `:server`) | `docker compose build` + smoke run (`curl /health`) |
| Swift lint (tasks touching Swift) | `swiftlint lint --config .swiftlint.yml` |
