# TASK_1 — Add a JVM target to `:shared`

Branch: `task/1-shared-jvm-target` · Depends on: — · Protocol: [AGENT.md](AGENT.md)

## Goal

Make `:shared` compile and test for plain JVM so the sync-server work can reuse the
Apple Music client, `Configuration`, the JWT developer-token signing, and the domain
models without touching Android or iOS behavior. Note: `AppleMusicAPI` and `URLSession`
are `internal` (module-scoped) — the server never touches them directly; the JVM-facing
entry point is `SyncEngine` in `:shared` jvmMain (TASK_5) plus the public
`Configuration` path added here.

## Context

- `shared/build.gradle.kts` currently declares only `android` and the three iOS targets.
  `:lastfmapi` and `:arkana` already have `jvm()` targets, so all of `:shared`'s
  common dependencies resolve on JVM.
- `commonMain` has 5 `expect` declarations that need JVM actuals:
  1. `MusicUserTokenProvider` (class) — `shared/src/commonMain/kotlin/dev/igorcferreira/musicstreamsync/domain/UserTokenProvider.kt`
  2. `createUserTokenProvider()` — same file
  3. `SystemLogger` (object) — `shared/src/commonMain/kotlin/dev/igorcferreira/musicstreamsync/domain/SystemLogger.kt`
  4. `MediaPlayerNativePlayer` (class) — `shared/src/commonMain/kotlin/dev/igorcferreira/musicstreamsync/domain/player/NativePlayer.kt`
  5. `buildNativePlayer()` — same file
  Reference implementations live in `shared/src/androidMain/.../domain/` and
  `shared/src/iosMain/.../domain/`.
- `JWTTokenSigner` (`shared/src/androidMain/kotlin/dev/igorcferreira/musicstreamsync/domain/JWTTokenSigner.kt`)
  is jjwt-based and JVM-compatible **except** `String.clearKey()`, which uses
  `android.util.Base64`.
- `URLSession` (`shared/src/commonMain/.../network/URLSession.kt`) builds a default-engine
  Ktor `HttpClient`, so jvmMain needs a client engine dependency. It currently
  `println`s **all request headers (including `Authorization` and `Music-User-Token`)
  and the full response body**, bypassing the `sanitizeHeader` config below it, and its
  catch-all `catch (ex: Exception)` also swallows `CancellationException`.
- `AppleMusicAPI` and `URLSession` are **`internal`**, and `Configuration`'s public
  constructors (`model/Configuration.kt`) hard-call `createUserTokenProvider()` — there
  is currently no public way to build a `Configuration` with an injected
  `UserTokenProvider`, which the multi-user server needs (one provider per user).
- `HTTPException` (`shared/src/commonMain/.../network/model/HTTPException.kt`) takes
  `code: Int` as a plain constructor parameter and **discards it** (not a `val`) —
  TASK_6's stale-token detection needs to read it.
- Version catalog: `libs.ktor.client.okhttp`, `libs.jjwt.api` / `libs.jjwt.impl`
  (+ `jjwt-orgjson` runtime, see androidMain deps for the pattern), `libs.kotlin.test`,
  `libs.kotlinx.coroutines.test` already exist in `gradle/libs.versions.toml`. Note
  `shared`'s `commonTest` already carries the jjwt trio.
- Existing test to reuse:
  `shared/src/androidHostTest/kotlin/dev/igorcferreira/musicstreamsync/model/JWTTokenSignerTests.kt`
  (full ES256 sign + verify round-trip). `shared/src/androidHostTest/kotlin/android/util/Base64.kt`
  is a shim of `android.util.Base64` onto `java.util.Base64` that becomes dead code once
  `clearKey()` moves off `android.util.Base64` — delete it then.
- The `swiftklib`/cinterop configuration is scoped to the iOS targets and must remain
  untouched. The `nativecoroutines` and `mokkery` plugins apply to all targets and are
  expected to work on JVM.

## Requirements

1. `jvm()` target added to `shared/build.gradle.kts` with `jvmTarget = JVM_17`
   (matching the android target).
2. `jvmMain` dependencies: a Ktor client engine (`libs.ktor.client.okhttp`), the jjwt
   trio (api + impl + orgjson runtime, without the Android `org.json` exclusion), and
   anything else required to compile.
3. JVM actuals:
   - `SystemLogger` — delegate to SLF4J if a logger dependency is already on the JVM
     classpath via Ktor, otherwise `println` with the same tag/message/throwable shape
     as the Android actual.
   - `MediaPlayerNativePlayer` / `buildNativePlayer()` — inert stub: never plays,
     reports `NOT_PLAYING`, emits no items. The server never uses the player; the stub
     exists only to satisfy the expect.
   - `MusicUserTokenProvider` / `createUserTokenProvider()` — a provider that wraps an
     **injected/settable token** (e.g. a mutable `var token: String?`). The expect
     declares a **no-arg constructor**, so the JVM actual must actualize it (the iOS
     actual is the precedent); a token-supplying constructor can only be an additional
     one. `getUserToken()` throws a clear exception when no token was injected. The
     JVM `createUserTokenProvider()` actual must **not** copy Android's singleton
     pattern (`MusicUserTokenProvider.shared`) — the server needs one provider instance
     per user, and a process-wide singleton silently breaks that.
4. **Public per-user construction path:** a public `Configuration` constructor (or
   factory) accepting a `UserTokenProvider` (e.g.
   `Configuration(developerToken, tokenSigner, userTokenProvider)`), so the server can
   build one `Configuration` per user without reaching for `internal` types.
   `AppleMusicAPI`/`URLSession` stay `internal`.
5. **`HTTPException` exposes its status:** change `code: Int` to `val code: Int`
   (additive, binary-compatible for iOS/Android). TASK_6's 401/403 handling depends on
   it.
6. **`URLSession` hygiene:** remove the debug `println`s of request headers and response
   bodies (the Ktor `Logging` plugin with its `sanitizeHeader` config stays), and make
   the catch-all rethrow `CancellationException` instead of wrapping it in
   `HTTPException`.
7. `JWTTokenSigner` available on JVM: extract the Android implementation into a source
   set shared between `androidMain` and `jvmMain` (e.g. an intermediate `jvmCommon`
   source set), replacing `android.util.Base64` with `java.util.Base64` or
   `kotlin.io.encoding.Base64`. Behavior on Android must not change (the existing
   Base64 handling of the Arkana-encoded private key — decode wrapper, strip PEM
   markers, decode content — must be preserved bit-for-bit). Delete the now-dead
   `androidHostTest` `android.util.Base64` shim.
8. A `jvmTest` source set with at least: a `JWTTokenSigner` signing test (port
   `JWTTokenSignerTests.kt` from `androidHostTest`, reusable nearly verbatim) and a
   `MusicUserTokenProvider` injection test.
9. No breaking API changes visible to iOS (additive changes like the new `Configuration`
   constructor and `HTTPException.code` are acceptable) and no behavior changes on
   Android.

## Non-goals

- No server module, no HTTP endpoints, no MongoDB (TASK_3/4).
- No sync logic (TASK_5).
- No changes to `:lastfmapi` (TASK_2).

## Validation

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
./gradlew ktlintCheck
./gradlew :shared:jvmTest
./gradlew :shared:testAndroidHostTest
./gradlew :composeApp:assembleDebug
./gradlew iosSimulatorArm64Test
```

All must pass. Delete `spec/PROGRESS.md`, then open a PR to `feature/kotlin-server/base`.
