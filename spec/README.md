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
| API docs | **Swagger mandate**: single `server/openapi.yaml`, updated in the same task as any endpoint change. |
| Branching | `feature/kotlin-server/base` is the integration branch; each task works on `task/<n>-<short-name>` and merges via PR. |

## Task table

Status values: `pending` / `in_progress` / `in_review` (PR open) / `done` (PR merged).

| ID | Title | Depends on | Status | Branch | PR | Spec |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | `:shared` JVM target + actuals | — | pending | — | — | [TASK_1_SPEC.md](TASK_1_SPEC.md) |
| 2 | lastfmapi session portability | — | pending | — | — | [TASK_2_SPEC.md](TASK_2_SPEC.md) |
| 3 | `:server` scaffold + Docker environment | — | pending | — | — | [TASK_3_SPEC.md](TASK_3_SPEC.md) |
| 4 | Multi-user token-sync API + persistence | 2, 3 | pending | — | — | [TASK_4_SPEC.md](TASK_4_SPEC.md) |
| 5 | Sync engine (diff + scrobble) | 1, 2 | pending | — | — | [TASK_5_SPEC.md](TASK_5_SPEC.md) |
| 6 | Scheduler loop + wiring | 4, 5 | pending | — | — | [TASK_6_SPEC.md](TASK_6_SPEC.md) |
| 7 | Shared push use case (`ServerSyncUseCase`) | 2, 4 | pending | — | — | [TASK_7_SPEC.md](TASK_7_SPEC.md) |
| 8 | Android app integration | 7 | pending | — | — | [TASK_8_SPEC.md](TASK_8_SPEC.md) |
| 9 | iOS app integration | 7 | pending | — | — | [TASK_9_SPEC.md](TASK_9_SPEC.md) |

### Dependency graph / parallelism

```
TASK_1 ─────────────┐
TASK_2 ──┬──────────┼──► TASK_5 ──┐
         │          │             ├──► TASK_6
TASK_3 ──┴► TASK_4 ─┴─────────────┘
                └─────► TASK_7 ──► TASK_8
                                └► TASK_9
```

- Wave 1 (parallel): TASK_1 ∥ TASK_2 ∥ TASK_3
- Wave 2 (parallel): TASK_4 (needs 2+3) ∥ TASK_5 (needs 1+2)
- Wave 3: TASK_6 (needs 4+5) ∥ TASK_7 (needs 2+4)
- Wave 4 (parallel): TASK_8 ∥ TASK_9 (need 7)

## Session log

<!-- One line per session: date — task — branch — what happened. -->
- 2026-07-09 — spec suite created (originally on `feature/kotlin_server`).
- 2026-07-09 — restructured into `feature/kotlin-server/base` (integration) + `feature/kotlin-server/spec` (this plan-approval branch).
