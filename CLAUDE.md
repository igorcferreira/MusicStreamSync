# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

MusicStreamSync is a Kotlin Multiplatform (KMP) application that syncs Apple Music playback history with Last.fm. It works as an intermediary between Apple Music and Last.fm for both iOS and Android platforms.

## Toolchain Versions

| Tool   | Version | Defined in                                 |
|--------|---------|--------------------------------------------|
| Java   | 21      | `.github/workflows/*.yml` (Temurin 21)     |
| Gradle | 9.8     | `gradle/wrapper/gradle-wrapper.properties` |
| AGP    | 9.4     | `gradle/libs.versions.toml` (`agp`)        |
| Kotlin | 2.4     | `gradle/libs.versions.toml` (`kotlin`)     |

Build with JDK 21 — newer JDKs are not supported by this Gradle/AGP combination.
Note that the modules compile to JVM 17 bytecode (`jvmTarget`/`compileOptions`),
which is independent of the JDK used to run the build.

## Build Commands

### Prerequisites
Before building, generate the secrets' module:
```bash
gem install arkana
arkana -l kotlin
```
Copy `.env.sample` to `.env` and fill in required values first.

### Common Commands
```bash
# Run all tests (JVM, iOS simulator)
./gradlew testAndroid iosSimulatorArm64Test

# Build Android app
./gradlew :composeApp:assembleDebug

# Build shared XCFramework for iOS
./gradlew :shared:assembleMusicStreamXCFramework

# Run specific Android test class
./gradlew :shared:testAndroidHostTest --tests "dev.igorcferreira.musicstreamsync.SomeTest"

# Run specific iOS test class
./gradlew :shared:iosSimulatorArm64Test --tests "dev.igorcferreira.musicstreamsync.SomeTest"
```

### Validating

**Always run the builds before committing.** Ensuring a build passes locally before commiting is vital for a health codebase

```shell
# Build Android app
./gradlew :composeApp:assembleDebug
```

Use the `iosApp` on `iosApp/iosApp.xcodeproj` and `xcrun` MCP tooling or `xcodebuild` command to build the iOS app.

### Linting

**Always run linters before committing.** Ktlint and SwiftLint are enforced in CI.

```bash
# Kotlin: check for violations
./gradlew ktlintCheck

# Kotlin: auto-fix violations
./gradlew ktlintFormat

# Swift: check for violations (requires: brew install swiftlint)
swiftlint lint --config .swiftlint.yml

# Swift: auto-fix violations
swiftlint --fix --config .swiftlint.yml
```

### Running the Apps
- **Android**: Use the `composeApp` run configuration in IntelliJ IDEA
- **iOS**: Open `iosApp/iosApp.xcodeproj` in Xcode, update Bundle ID and Team ID in `App.xcconfig`, then run

## Architecture

### Module Structure

- **`shared`**: Core KMP library containing business logic, domain models, and API clients
  - `commonMain`: Cross-platform code (domain layer, network, models)
  - `androidMain`/`iosMain`: Platform-specific implementations
  - `native/`: Swift packages for iOS-specific bridges (MusicKitBridge, OSLogger)

- **`composeApp`**: Android-only Compose UI application
  - MVVM architecture with ViewModels per feature (player, history, playlist, lastfm)
  - DI via `ViewModelFactory`

- **`lastfmapi`**: Standalone KMP module for Last.fm API client

- **`arkana`**: Generated secrets module (created by running `arkana -l kotlin`)

- **`mediaplayback`/`musickitauth`**: Pre-built Android AARs for MusicKit integration

- **`swift-klib-plugin`**: Gradle plugin used to compiled Swift Packages into KMP
  - This plugin is responsible for the compilation of the packages under `shared/native/`
  - Added to this project as a git sub-module

### Key Architectural Patterns

**Domain Layer** (`shared/src/commonMain/kotlin/dev/igorcferreira/musicstreamsync/domain/`):
- `UseCase` / `ResultUseCase`: Base use case abstractions
- `Scrobbler`: Interface for Last.fm scrobbling
- `NativePlayer`: Platform-agnostic player abstraction
- `TokenSigner` / `UserTokenProvider`: MusicKit authentication

**iOS Native Integration**:
- Uses `swift-klib-plugin` plugin to compile Swift packages into cinterop bindings
- `MusicKitBridge`: Swift package bridging MusicKit APIs to Kotlin
- `MediaPlayer.def`: C-interop definition for iOS MediaPlayer framework

**Secrets Management**:
- Uses Arkana to obfuscate API keys at compile time
- Required secrets: `TeamId`, `KeyId`, `PrivateKey` (MusicKit), `LastFMAPIKey`, `LastFMAPISecret`

### Network Layer
- Ktor client for HTTP (Darwin engine on iOS, OkHttp on Android)
- `AppleMusicAPI`: Apple Music REST API client
- `LastFMClient`: Last.fm scrobbling API client

## Testing

Tests are located in:
- `shared/src/commonTest/kotlin/` - Cross-platform tests
- `shared/src/androidHostTest/kotlin/` - Android-specific tests (JVM host)
- `shared/src/iosTest/kotlin/` - iOS-specific tests

Uses Mokkery for mocking in tests.
