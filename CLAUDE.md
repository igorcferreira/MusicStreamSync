# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

MusicStreamSync is a Kotlin Multiplatform (KMP) application that syncs Apple Music playback history with Last.fm. It works as an intermediary between Apple Music and Last.fm for both iOS and Android platforms.

## Build Commands

### Prerequisites
Before building, generate the secrets module:
```bash
gem install arkana
arkana -l kotlin
```
Copy `.env.sample` to `.env` and fill in required values first.

### Common Commands
```bash
# Run all tests (JVM, iOS simulator, macOS)
./gradlew test macosX64Test iosSimulatorArm64Test

# Build Android app
./gradlew :composeApp:assembleDebug

# Build shared XCFramework for iOS
./gradlew :shared:assembleMusicStreamXCFramework

# Run specific test class
./gradlew :shared:testDebugUnitTest --tests "dev.igorcferreira.musicstreamsync.SomeTest"
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

### Key Architectural Patterns

**Domain Layer** (`shared/src/commonMain/kotlin/dev/igorcferreira/musicstreamsync/domain/`):
- `UseCase` / `ResultUseCase`: Base use case abstractions
- `Scrobbler`: Interface for Last.fm scrobbling
- `NativePlayer`: Platform-agnostic player abstraction
- `TokenSigner` / `UserTokenProvider`: MusicKit authentication

**iOS Native Integration**:
- Uses `swiftklib` plugin to compile Swift packages into cinterop bindings
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
- `shared/src/androidUnitTest/kotlin/` - Android-specific tests
- `shared/src/iosTest/kotlin/` - iOS-specific tests

Uses Mokkery for mocking in tests.