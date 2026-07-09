# TASK_6 — Scheduler loop + wiring + resilience

Branch: `task/6-scheduler` · Depends on: TASK_4, TASK_5 · Protocol: [AGENT.md](AGENT.md)

## Goal

Turn the pieces into the running product: a cron-style coroutine loop that, on a
configurable interval, iterates **all registered users** and runs `SyncEngine.sync(user)`
for each, with per-user failure isolation and operational visibility.

## Context

- `SYNC_INTERVAL_MINUTES` env config exists since TASK_3 (default 5).
- `UserStore` (TASK_4) lists users and holds per-user tokens, session, cursor,
  `tokenStale`, and the sync-run log. `SyncResult` comes from TASK_5.
- Per-user clients: for each user the loop builds an `AppleMusicAPI` (JVM
  `MusicUserTokenProvider` injected with that user's Music-User-Token, developer token
  from Arkana + `JWTTokenSigner` — TASK_1) and a `LastFMClient` restored from the
  user's stored `Session` (TASK_2, with the per-user `Settings` isolation chosen
  there).
- `HTTPException` types exist in both `shared` (`network/model/HTTPException.kt`, carries
  the status code) and `lastfmapi` — use them to classify failures.

## Requirements

1. **Scheduler:** a coroutine loop started with the Ktor application (and cancelled on
   shutdown — hook `ApplicationStopping` / structured `CoroutineScope` tied to the
   server lifecycle). Interval from `SYNC_INTERVAL_MINUTES`. Runs are strictly
   sequential per instance (no overlapping ticks: if a run outlives the interval, the
   next tick waits or is skipped — pick one, log it).
2. **Per-user run:** each tick lists users from `UserStore` and for each user:
   - Skip with a log line when preconditions are missing (`tokenStale == true`, no
     Last.fm session yet) — never throws.
   - Build the per-user clients, call `SyncEngine.sync(user)`, append the `SyncResult`
     to the user's sync-run log and `lastSync` field.
   - **Isolation:** any exception is caught, logged (tagged with the user id — never the
     raw token), recorded in the user's log, and the loop continues with the next user.
3. **Stale-token detection:** an Apple Music 401/403 marks the user's `tokenStale = true`
   in `UserStore`; the user is skipped until an app pushes a fresh token
   (`PUT /api/users/tokens/apple-music` clears the flag — TASK_4 behavior). Surfaced by
   `GET /api/users/status` so the apps can prompt the user.
4. **Observability:** structured log lines per tick (users processed / scrobbled /
   skipped / failed) and per user; `/health` may optionally expose the last tick time.
   If any HTTP surface changes, update `server/openapi.yaml` (mandate).
5. **Graceful shutdown:** in-flight user sync finishes or cancels cleanly; no orphaned
   coroutines (verify via test scope).
6. **Tests:** virtual-time (`kotlinx-coroutines-test`) loop tests with a fake
   `UserStore` + fake engine: multiple users where one fails and others still sync;
   stale-token marking on 401; interval pacing; shutdown cancellation. A compose-level
   smoke run proves end-to-end wiring (with fake tokens it logs a failed/skipped sync
   rather than crashing).

## Non-goals

- No new app-facing endpoints (unless `/health` is extended — then update
  `openapi.yaml`).
- No retry/backoff sophistication beyond "next tick retries" — keep it simple.
- No client-side work (TASK_7/8/9).

## Validation

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
./gradlew ktlintCheck
./gradlew :server:test
docker compose build
docker compose up -d && sleep 5 && docker compose logs server | grep -i "sync tick" && docker compose down
```

Delete `spec/PROGRESS.md`, then open a PR to `feature/kotlin-server/base`.
