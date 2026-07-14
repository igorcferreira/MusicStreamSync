# TASK_7 — Shared push use case (`ServerSyncUseCase`)

Branch: `task/7-server-sync-usecase` · Depends on: TASK_1, TASK_2, TASK_4 · Protocol: [AGENT.md](AGENT.md)

## Goal

A shared (commonMain) use case both apps call to push the device-minted credentials to
the server: the Apple Music **Music-User-Token** and the exported Last.fm **Session**.
This is the only piece of client networking against the server API.

## Context

- Server contract: `server/openapi.yaml` (TASK_4) —
  `PUT /api/users/tokens/apple-music`, `PUT /api/users/tokens/lastfm-session`,
  `GET /api/users/status`; bearer shared-secret auth + `Music-User-Token` header.
  **The OpenAPI document is authoritative** — read it before implementing.
- Existing pieces to reuse:
  - `UserTokenProvider.getUserToken(developerToken)` and
    `Configuration`/`DeveloperToken` + `TokenSigner` for minting/obtaining the
    Music-User-Token on device
    (`shared/src/commonMain/.../domain/UserTokenProvider.kt`, `model/Configuration.kt`).
  - `LastFMClient` session export from TASK_2 (via the `Scrobbler`/`LastFMUseCase`
    layer — expose the current session through `Scrobbler` rather than constructing a
    second `LastFMClient`).
  - `IURLSession`/`URLSession` (`shared/src/commonMain/.../network/URLSession.kt`) for
    HTTP — extend if it lacks request bodies (it currently only performs body-less
    requests), or add a small dedicated Ktor client; prefer extending `IURLSession` so
    tests can fake it like the rest of the codebase.
  - `UseCase`/`ResultUseCase` base classes and the existing use-case patterns in
    `shared/src/commonMain/.../domain/use_cases/`.
  - `multiplatform-settings` (already a commonMain dependency) for persisting
    configuration.
- Consumers: TASK_8 (Android ViewModel) and TASK_9 (iOS SwiftUI) — the API surface must
  be Swift-friendly (`@NativeCoroutinesState`/`@Throws` conventions as used in
  `LastFMUseCase`).

## Requirements

1. **Configuration storage:** server base URL + shared secret persisted via
   multiplatform-settings (secret ideally in secure storage where available — KVault is
   already an androidMain dependency; keep the common interface simple:
   `ServerConfigurationStore` with get/set/clear).
2. **`ServerSyncUseCase`** (commonMain), constructor-injected dependencies, exposing:
   - `suspend fun pushTokens()`: obtains the Music-User-Token via the existing
     `UserTokenProvider` (may trigger the on-device MusicKit auth flow on first use),
     PUTs it to `/api/users/tokens/apple-music`; then, if a Last.fm session exists
     (TASK_2 export), PUTs it to `/api/users/tokens/lastfm-session`. Partial success is
     reported distinctly (token pushed, session missing).
   - `suspend fun fetchStatus()`: calls `GET /api/users/status` and maps to a small
     model (registered, hasLastFmSession, tokenStale, lastSync).
   - Observable state for UI (`StateFlow` + `@NativeCoroutinesState`, mirroring
     `LastFMUseCase.isAuthenticated`): idle / syncing / success / error(message).
3. **Headers:** `Authorization: Bearer <secret>` + `Music-User-Token` per the OpenAPI
   contract; if TASK_4 implemented `previousTokenHash` migration, compute and send it
   (persist the last-pushed token hash in settings).
4. **Errors:** network/HTTP failures surface as state (and `@Throws`-compatible
   exceptions for iOS), never crash; secrets never logged.
5. **Tests** (commonTest, Mokkery + faked `IURLSession`): happy path (both PUTs, correct
   headers/bodies), token-only partial push, 401 (bad secret) and 404 (unregistered
   status) mapping, state transitions.

## Non-goals

- No UI (TASK_8/9).
- No server changes; if the contract is wrong, fix it via a TASK_4 follow-up, not here.

## Validation

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
./gradlew ktlintCheck
./gradlew :shared:testAndroidHostTest :shared:jvmTest
./gradlew iosSimulatorArm64Test
./gradlew :composeApp:assembleDebug
```

Optional end-to-end check: `docker compose up` the server and run `pushTokens()` from a
JVM test against `localhost` with a test secret. Delete `spec/PROGRESS.md`, then open a
PR to `feature/kotlin-server/base`.
