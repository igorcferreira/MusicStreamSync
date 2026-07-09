# TASK_9 — iOS app integration

Branch: `task/9-ios-integration` · Depends on: TASK_7 · Protocol: [AGENT.md](AGENT.md)

## Goal

Same capability as TASK_8, on iOS: a SwiftUI settings surface in `iosApp` that
configures the sync server and pushes the user's tokens via the shared
`ServerSyncUseCase` through the generated `MusicStream` framework.

## Context

- `iosApp/iosApp.xcodeproj` consumes the shared code as the `MusicStream` XCFramework
  (`./gradlew :shared:assembleMusicStreamXCFramework`). Coroutine state is exposed to
  Swift via KMP-NativeCoroutines (`@NativeCoroutinesState` — see how existing screens
  observe `LastFMUseCase.isAuthenticated`).
- `ServerSyncUseCase` (TASK_7) exposes `pushTokens()`, `fetchStatus()`, and an
  observable state; `@Throws` functions surface as Swift `throws`.
- On iOS the Music-User-Token comes from MusicKit via the `MusicKitBridge` Swift
  package; the shared `UserTokenProvider` iOS actual already handles it —
  `pushTokens()` may trigger the MusicKit permission prompt on first use.
- Follow existing SwiftUI patterns in `iosApp` (view models/observable wrappers around
  shared use cases, `App.xcconfig` for configuration) and SwiftLint
  (`.swiftlint.yml`).

## Requirements

1. **Server settings view** (section in the existing settings/Last.fm area, matching the
   app's navigation): server URL field, shared secret field (secure entry), "Sync tokens
   to server" action, and a status area driven by the use case state + `fetchStatus()`
   result (registered, Last.fm session present, token-stale warning with a re-sync
   prompt).
2. **Observation:** state consumed through the KMP-NativeCoroutines Swift APIs the
   project already uses — no polling.
3. **Errors:** failures render as user-visible messages (matching existing error
   presentation), never crash; the secret is never printed/logged.
4. **Framework surface:** if any shared API turns out not Swift-usable, fix it via a
   TASK_7 follow-up (do not fork logic into Swift).

## Non-goals

- No Android work (TASK_8).
- No new shared business logic.

## Validation

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
./gradlew ktlintCheck
./gradlew :shared:assembleMusicStreamXCFramework
./gradlew iosSimulatorArm64Test
swiftlint lint --config .swiftlint.yml
# Build the app (per root CLAUDE.md): xcodebuild or xcrun MCP tooling on iosApp/iosApp.xcodeproj
```

Manual check: run in the simulator against a locally running server
(`docker compose up`), configure URL + secret, push tokens, confirm via
`GET /api/users/status`. Delete `spec/PROGRESS.md`, then open a PR to
`feature/kotlin_server`.
