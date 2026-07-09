# TASK_1 — Add a JVM target to `:shared`

Branch: `task/1-shared-jvm-target` · Depends on: — · Protocol: [AGENT.md](AGENT.md)

## Goal

Make `:shared` compile and test for plain JVM so the future `:server` module can reuse
`AppleMusicAPI`, `Configuration`, the JWT developer-token signing, and the domain models
without touching Android or iOS behavior.

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
  Ktor `HttpClient`, so jvmMain needs a client engine dependency.
- Version catalog: `libs.ktor.client.okhttp`, `libs.jjwt.api` / `libs.jjwt.impl`
  (+ `jjwt-orgjson` runtime, see androidMain deps for the pattern), `libs.kotlin.test`,
  `libs.kotlinx.coroutines.test` already exist in `gradle/libs.versions.toml`.
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
     **injected/settable token** (e.g. a mutable `var token: String?` or
     constructor-supplied lambda). `getUserToken()` throws a clear exception when no
     token was injected. The server (TASK_4/5) constructs per-user
     `AppleMusicAPI`/`Configuration` instances, each with its own provider instance.
4. `JWTTokenSigner` available on JVM: extract the Android implementation into a source
   set shared between `androidMain` and `jvmMain` (e.g. an intermediate `jvmCommon`
   source set), replacing `android.util.Base64` with `java.util.Base64` or
   `kotlin.io.encoding.Base64`. Behavior on Android must not change (the existing
   Base64 handling of the Arkana-encoded private key — decode wrapper, strip PEM
   markers, decode content — must be preserved bit-for-bit).
5. A `jvmTest` source set with at least: a `JWTTokenSigner` signing test (mirroring the
   existing Android test if one exists, otherwise a new ES256 round-trip using jjwt's
   parser) and a `MusicUserTokenProvider` injection test.
6. No public API changes visible to iOS (the generated `MusicStream` framework surface
   is unchanged) and no behavior changes on Android.

## Non-goals

- No server module, no HTTP endpoints, no MongoDB (TASK_3/4).
- No sync logic (TASK_5).
- No changes to `:lastfmapi` (TASK_2).

## Validation

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
./gradlew ktlintCheck
./gradlew :shared:jvmTest
./gradlew :shared:testAndroidHostTest
./gradlew :composeApp:assembleDebug
./gradlew iosSimulatorArm64Test
```

All must pass. Delete `spec/PROGRESS.md`, then open a PR to `feature/kotlin_server`.
