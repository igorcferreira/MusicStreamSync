# MusicStreamSync — Sync Server spec suite

Orchestration file for building the **sync server**: a Kotlin/JVM process built on the
existing KMP shared code that runs a cron-style loop to sync Apple Music play history
into Last.fm, for multiple users. Working sessions follow the protocol in
[AGENT.md](AGENT.md).

## Problem statement

The mobile apps only scrobble while they are running. The server closes that gap: on a
time interval it fetches each registered user's Apple Music recently-played list,
compares it with their Last.fm track history, and scrobbles the songs that are missing.

Target behavior (the canonical scenario, encoded as acceptance tests in TASK_5):

- User listens to Song A
- User listens to Song B
- Time passes. Cron job triggers → adds Song A and Song B to Last.fm history
- User listens to Song C
- Time passes. Cron job triggers → adds Song C to Last.fm history
- User listens to Song A again
- Time passes. Cron job triggers → adds Song A to Last.fm history
- Time passes. Cron job triggers → adds nothing

Apple Music has no server-side login or token refresh. The native apps therefore mint
the **Music-User-Token** on-device and push it (together with the user's Last.fm session
key) to the server.

## Architecture

```
┌─────────────┐   ┌─────────────┐
│ Android app │   │   iOS app   │      mint Music-User-Token on device
│ (composeApp)│   │  (iosApp)   │      authenticate with Last.fm on device
└──────┬──────┘   └──────┬──────┘
       │  ServerSyncUseCase (shared, TASK_7)
       │  PUT /api/users/tokens/apple-music
       │  PUT /api/users/tokens/lastfm-session
       ▼
┌────────────────────────────────────────────────┐
│ :server (Ktor, JVM, Docker)                    │
│                                                │
│  Token API (TASK_4) ──► UserStore ◄─┐          │
│                          (MongoDB)  │          │
│  Scheduler loop (TASK_6)            │          │
│    for each registered user:        │          │
│      SyncEngine.sync(user) (TASK_5)─┘          │
│        │ getRecentlyPlayed()   │ diff + delta  │
│        ▼                       ▼               │
│  AppleMusicAPI (:shared jvm)  LastFMClient     │
│        │        TASK_1        (:lastfmapi jvm) │
└────────┼───────────────────────────┼───────────┘
         ▼                           ▼
   Apple Music API              Last.fm API
   (recently played)       (getRecentTracks, scrobble)
```

## Decisions record

| Decision | Choice |
| --- | --- |
| User model | **Multi-user**: one server instance serves many users. User id is resolved server-side from the Music-User-Token / Apple Music API (mechanism fixed in TASK_4, with a token-derived-hash fallback). |
| Last.fm auth | **Apps push the session key** (apps authenticate on-device via `LastFMClient`, export the `Session`, upload it). No Last.fm credentials on the server. |
| Persistence | **MongoDB** (docker-compose service, official Kotlin coroutine driver): per-user token, session, sync cursor, sync-run log. |
| Environment | **Docker mandate**: dev and deploy via `Dockerfile` + `docker-compose.yml` (server + mongodb). |
| API docs | **OpenAPI generation mandate**: a single `server/openapi.yaml` served at `GET /openapi.yaml`, **generated from the code's route definitions** (code is the source of truth; summaries, schemas, and examples live on the routes) with a CI drift check on the checked-in snapshot. Established by TASK_10 **before** the token API (TASK_4), so endpoints are documented in-code from TASK_4 on; TASK_3's initial document is hand-written and TASK_10 migrates it to generation. |
| Branching | `feature/kotlin-server/base` is the integration branch; each task works on `task/<n>-<short-name>` and merges via PR. |

## Pre-PR review mandate

After a task is finished, **before opening a PR**, start a sub-agent to behave as a
**Senior Kotlin Developer** to review the changes made, using the task's spec file
(`TASK_N_SPEC.md`) as reference, and give feedback, like a PR review. After the
feedback is provided, fix the issues raised (re-running the task's validation matrix),
then open the PR. If a PR is already open when feedback arrives, push the fixes to the
PR branch.

## Commit mandate

When creating commits for the changes, create **atomic commits**, not a single commit:
each commit is one self-contained logical change (e.g. version-catalog additions,
module scaffold, a feature slice, its tests, Docker files, spec bookkeeping), with a
message explaining that change. This refines the "at least one commit per task" rule
in [AGENT.md](AGENT.md).

## Task table

Status values: `pending` / `in_progress` / `in_review` (PR open) / `done` (PR merged).

| ID | Title | Depends on | Status | Branch | PR | Spec |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | `:shared` JVM target + actuals | — | done | `task/1-shared-jvm-target` | [#82](https://github.com/igorcferreira/MusicStreamSync/pull/82) | [TASK_1_SPEC.md](TASK_1_SPEC.md) |
| 2 | lastfmapi session portability | — | done | `task/2-lastfm-session-portability` | [#83](https://github.com/igorcferreira/MusicStreamSync/pull/83) | [TASK_2_SPEC.md](TASK_2_SPEC.md) |
| 3 | `:server` scaffold + Docker environment | — | in_review | `task/3-server-scaffold` | [#84](https://github.com/igorcferreira/MusicStreamSync/pull/84) | [TASK_3_SPEC.md](TASK_3_SPEC.md) |
| 4 | Multi-user token-sync API + persistence | 2, 3, 10 | pending | — | — | [TASK_4_SPEC.md](TASK_4_SPEC.md) |
| 5 | Sync engine (diff + scrobble) | 1, 2 | pending | — | — | [TASK_5_SPEC.md](TASK_5_SPEC.md) |
| 6 | Scheduler loop + wiring | 4, 5 | pending | — | — | [TASK_6_SPEC.md](TASK_6_SPEC.md) |
| 7 | Shared push use case (`ServerSyncUseCase`) | 1, 2, 4 | pending | — | — | [TASK_7_SPEC.md](TASK_7_SPEC.md) |
| 8 | Android app integration | 7 | pending | — | — | [TASK_8_SPEC.md](TASK_8_SPEC.md) |
| 9 | iOS app integration | 7 | pending | — | — | [TASK_9_SPEC.md](TASK_9_SPEC.md) |
| 10 | Generate `openapi.yaml` from code | 3 | pending | — | — | [TASK_10_SPEC.md](TASK_10_SPEC.md) |

### Dependency graph / parallelism

```
TASK_1 ──┬─────────────────────► TASK_5 ──┐
TASK_2 ──┤                                 ├──► TASK_6
         │                                 │
TASK_3 ──┴► TASK_10 ──► TASK_4 ────────────┴──► TASK_7 ──┬──► TASK_8
                                                         └──► TASK_9
```

Edges show the critical path; TASK_4 also needs 2, TASK_7 also needs 1+2 (the
`Depends on` column is authoritative). TASK_10 establishes OpenAPI generation from the
`/health` surface before TASK_4 adds the token API, so the token endpoints are
documented in-code from the start.

- Wave 1 (parallel): TASK_1 ∥ TASK_2 ∥ TASK_3
- Wave 2 (parallel): TASK_5 (needs 1+2) ∥ TASK_10 (needs 3 — establish OpenAPI
  generation from `/health`)
- Wave 3: TASK_4 (needs 2+3+10 — token API, documented in-code via the generation setup)
- Wave 4 (parallel): TASK_6 (needs 4+5) ∥ TASK_7 (needs 1+2+4)
- Wave 5 (parallel): TASK_8 ∥ TASK_9 (need 7)

## Session log

<!-- One line per session: date — task — branch — what happened. -->
- 2026-07-09 — spec suite created (originally on `feature/kotlin_server`).
- 2026-07-09 — restructured into `feature/kotlin-server/base` (integration) + `feature/kotlin-server/spec` (this plan-approval branch).
- 2026-07-09 — senior Kotlin review of PR #81 applied: fixed internal-visibility design (SyncEngine → `:shared` jvmMain, public `Configuration` path), `Session.subscriber` wire shape, `HTTPException.code` exposure, top-K prefix cursor, Arkana JDK-21 Docker constraint, token-logging/cancellation hygiene, multi-device identity caveat, Mongo field-level update semantics.
- 2026-07-10 — TASK_1 — `task/1-shared-jvm-target` — implemented `:shared` JVM target, `jvmCommon` JWTTokenSigner, JVM actuals, public per-user `Configuration` constructor, `HTTPException.code`, `URLSession` hygiene; full validation matrix green; PR opened.
- 2026-07-10 — TASK_1 — senior review round applied to PR #82 (clearKey whitespace parity, explicit slf4j-api, @Volatile token, Configuration/HTTPException tests); CI green; merged → done.
- 2026-07-10 — TASK_2 — `task/2-lastfm-session-portability` — session export/import (`currentSession`, session constructor + `restoreSession`, instance-scoped via internal `InMemorySettings`), `Session.subscriber` default, now-playing-tolerant `Track`/`listLatestTracks`, `scrobble` exception hygiene; PR opened.
- 2026-07-10 — protocol — added the pre-PR senior-Kotlin-developer sub-agent review mandate to this README; ran it on TASK_2 and applied the findings (restoreSession scoping docs, thread-safe InMemorySettings, blank-key fail-fast, evidence-grade now-playing fixture) on the PR #83 branch.
- 2026-07-10 — TASK_2 — merged → done; also added the atomic-commit mandate to this README.
- 2026-07-10 — TASK_3 — `task/3-server-scaffold` — scaffolded the `:server` Ktor/JVM module (env `ServerConfig`, MongoDB coroutine client, DI `DatabasePinger`, `GET /health`), the single `server/openapi.yaml` served at `GET /openapi.yaml`, `:server:test` suites, and the Docker environment (multi-stage JDK21 build + JRE21 runtime, `docker-compose.yml`); `docker compose up` smoke passed. Extended CI (`test.yml`) to run the lastfmapi + server JVM tests. Rebased onto merged base; atomic commits; PR opened.
- 2026-07-10 — spec — added the OpenAPI-generation goal: `server/openapi.yaml` will be generated from the code's route definitions (new TASK_10), reworked the API-docs mandate (AGENT.md) and the decisions record accordingly.
- 2026-07-10 — spec — retargeted TASK_10 to run **before** TASK_4 (TASK_10 now needs only 3; TASK_4 gains a dependency on 10), so the token API is documented in-code from the start; updated the graph/waves and both task specs.
