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
  is already `@Serializable` (verify: it must serialize `name` and `key` for transport).
- The module already targets `jvm()` — no build changes expected.
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
4. `Session` stays `@Serializable` and its wire shape (`name`, `key`) is documented —
   TASK_4 reuses it as the upload payload schema.
5. Existing behavior unchanged: mobile flow (`authenticate` → stored in default
   `Settings`) keeps working; `logout()` clears whatever storage backs the client.
6. iOS framework surface: new API must be usable from Swift (no `internal` leakage,
   consider `@Throws` where needed) since TASK_7/9 call the export from shared/iOS code.

## Non-goals

- No server code, no HTTP transport (TASK_4/7 handle transport).
- No changes to scrobbling or track-listing logic.

## Validation

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
./gradlew ktlintCheck
./gradlew :lastfmapi:jvmTest          # includes the new round-trip + isolation tests
./gradlew :composeApp:assembleDebug
./gradlew iosSimulatorArm64Test
```

Required test: **round-trip** — authenticate against a faked `API`/`Settings` (or inject
a prebuilt `Session`), export, import into a fresh client, assert
`isAuthenticated == true` and that a scrobble call signs with the imported key.
Delete `spec/PROGRESS.md`, then open a PR to `feature/kotlin_server`.
