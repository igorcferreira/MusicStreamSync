# TASK_8 — Android app integration

Branch: `task/8-android-integration` · Depends on: TASK_7 · Protocol: [AGENT.md](AGENT.md)

## Goal

Let the Android app configure the sync server and push the user's tokens to it: a
settings surface in `composeApp` wired to `ServerSyncUseCase`.

## Context

- `composeApp` is MVVM: one ViewModel per feature (player, history, playlist, lastfm)
  constructed via `ViewModelFactory` — find them under
  `composeApp/src/**/dev/igorcferreira/musicstreamsync/` and follow the existing
  screen/ViewModel/navigation patterns exactly (study the Last.fm login screen, which is
  the closest analog: form + async action + state display).
- `ServerSyncUseCase` + `ServerConfigurationStore` (TASK_7) provide all logic and state;
  this task is UI + wiring only.
- The MusicKit auth flow may be triggered by `pushTokens()` on first use (the Android
  `MusicUserTokenProvider` launches an activity result) — the action must be triggered
  from a foreground Activity context, like existing flows.

## Requirements

1. **Server settings screen** (or section within the existing settings/Last.fm area,
   matching current navigation): fields for server URL and shared secret (masked),
   persisted through `ServerConfigurationStore`; a "Sync tokens to server" button; a
   status area rendering the use case's state flow (idle/syncing/success/error) and the
   result of `fetchStatus()` (registered, Last.fm session present, token stale warning).
2. **ViewModel** (`ServerSyncViewModel` or matching naming), created via
   `ViewModelFactory` like its peers, delegating to `ServerSyncUseCase` — no business
   logic in the composables.
3. **Token-stale handling:** when status reports `tokenStale`, show a prompt to re-sync
   (the push clears the flag server-side).
4. **Tests:** ViewModel unit test with a mocked use case (state propagation, action
   dispatch). UI tests optional, matching whatever pattern already exists in
   `composeApp`.

## Non-goals

- No iOS work (TASK_9).
- No changes to `:shared` beyond what TASK_7 delivered (if the use case API is
  insufficient, do a TASK_7 follow-up first).

## Validation

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
./gradlew ktlintCheck
./gradlew :composeApp:assembleDebug
./gradlew :shared:testAndroidHostTest
```

Manual check: run `composeApp` against a locally running server (`docker compose up`),
configure URL + secret, push tokens, confirm the user appears via
`GET /api/users/status`. Delete `spec/PROGRESS.md`, then open a PR to
`feature/kotlin-server/base`.
