# TASK_2 — Last.fm session portability (`:lastfmapi`)

Branch: `task/2-lastfm-session-portability` · Depends on: — · Protocol: [AGENT.md](AGENT.md)

## Goal

Let a `Session` obtained on one device travel to another process: the apps authenticate
with Last.fm on-device and **export** the session; the server **imports** it and uses a
fully authenticated `LastFMClient` without ever seeing the user's password.

## Context

- `lastfmapi/src/commonMain/kotlin/dev/igorcferreira/lastfm/LastFMClient.kt`:
  - `authenticate(username, password)` returns the `Session` and stores it via the
    **internal** `Settings.session` extension (`API.SESSION_KEY`).
  - The only public constructor is `LastFMClient(apiKey, secret)`, which uses the no-arg
    `Settings()`; the `Settings`-taking constructor is `internal`.
  - `isAuthenticated`, `listLatestTracks`, `scrobble`, `updateNowPlaying` all read the
    stored session through `API` / `settings.session`.
- `Session` (`lastfmapi/src/commonMain/kotlin/dev/igorcferreira/lastfm/model/Session.kt`)
  is `@Serializable data class Session(name, key, subscriber: Int)` — **`subscriber` is
  required with no default**, so a `{name, key}` payload does not deserialize today.
- `LastFMClient.scrobble` wraps **every** exception — including `CancellationException`
  — into `HTTPException(InternalServerError)` (`LastFMClient.kt`, the catch in
  `scrobble`), flattening real error codes to 500 and breaking structured concurrency.
- `Track` (`model/Track.kt`) declares `@SerialName("date") val uts: TrackDate`
  **non-nullable with no default**, but Last.fm's `user.getRecentTracks` returns the
  currently-playing track *without* a `date` attribute (`@attr nowplaying`), so
  `listLatestTracks()` throws `MissingFieldException` whenever the user is mid-listen.
- The module already targets `jvm()` — no build changes expected, except adding
  `com.russhwolf:multiplatform-settings-test` to `gradle/libs.versions.toml` (version
  ref `multiplatformSettings` exists) if `MapSettings` is used for isolation/tests.
- Consumers: TASK_4 (server imports a session per user), TASK_7 (apps export the session
  to upload it).

## Requirements

1. **Export:** a public way to read the currently stored session, e.g.
   `val LastFMClient.currentSession: Session?` or `fun currentSession(): Session?`.
   Returns `null` when not authenticated.
2. **Import:** a public way to build/restore an authenticated client from a transported
   session, e.g. `fun LastFMClient.restoreSession(session: Session)` and/or a public
   factory `LastFMClient(apiKey, secret, session)`. After import,
   `isAuthenticated == true` and `listLatestTracks()`/`scrobble()` use the imported
   session key exactly as if `authenticate()` had run locally.
3. **Isolation for multi-user servers:** the server will hold one client per user in one
   process. The no-arg `Settings()` is process-global, so restored sessions must not
   collide: either make the `Settings`-taking constructor public (server passes an
   in-memory `Settings` per user, e.g. `MapSettings` from
   `multiplatform-settings-test`, or an own trivial implementation) or make the imported
   session instance-scoped. Pick one, document it in KDoc, and cover it with a test
   (two clients with different sessions in one process don't cross-talk).
4. **Wire shape:** give `Session.subscriber` a default (`subscriber: Int = 0`) so a
   `{name, key}` payload deserializes, and document the full wire shape
   (`name`, `key`, optional `subscriber`) — TASK_4 reuses it as the upload payload
   schema and must match it in `openapi.yaml`.
5. **Now-playing resilience:** make `Track`'s date field nullable (or defaulted) and
   have `listLatestTracks()` tolerate/filter the timestamp-less now-playing entry.
   Test with a `getRecentTracks` fixture containing an `@attr nowplaying` track —
   without this, TASK_5's history cross-check fails exactly while the user is
   listening.
6. **Exception hygiene in `scrobble`:** rethrow `CancellationException` and preserve
   the original `HTTPException` code instead of flattening everything to
   `InternalServerError` — TASK_6 classifies failures by status code.
7. Existing behavior unchanged: mobile flow (`authenticate` → stored in default
   `Settings`) keeps working; `logout()` clears whatever storage backs the client.
8. Swift reachability: the `MusicStream` framework does **not** export `:lastfmapi`
   (no `export(project(":lastfmapi"))` in `shared`'s framework block), so "usable from
   Swift" means usable from shared commonMain code that is itself exported — the
   export surface the apps actually consume is the `Scrobbler`/`LastFMUseCase` wrapper
   that TASK_7 extends. Keep new API `public` in commonMain and free of `internal`
   types in signatures.

## Non-goals

- No server code, no HTTP transport (TASK_4/7 handle transport).
- No changes to scrobbling or track-listing *behavior* beyond the error-handling and
  now-playing fixes scoped above.

## Validation

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
./gradlew ktlintCheck
./gradlew :lastfmapi:jvmTest          # includes the new round-trip + isolation tests
./gradlew :composeApp:assembleDebug
./gradlew iosSimulatorArm64Test
```

Required test: **round-trip** — authenticate against a faked `API`/`Settings` (or inject
a prebuilt `Session`), export, import into a fresh client, assert
`isAuthenticated == true` and that a scrobble call signs with the imported key.
Delete `spec/PROGRESS.md`, then open a PR to `feature/kotlin-server/base`.
