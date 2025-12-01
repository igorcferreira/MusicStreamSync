# MusicStreamSync

The `MusicStreamSync` is an application which allows the sync between Apple Music and Last.fm.

Apple Music does not allow a full integration with Last.fm because of the format of the APIs.

This app connects with Apple Music to list the current playing item and the history to work as an
intermediary between Apple and Last.fm, for both iOS and Android.

| iOS                        | Android                            |
|----------------------------|------------------------------------|
| ![iOS Demo](docs/ios.webp) | ![Android Demo](docs/android.webp) |

## Project structure

This is a Kotlin Multiplatform project targeting Android, iOS.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
    - `commonMain` is for code that’s common for all targets.
    - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.

* `/iosApp` contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* `/shared` is for the code that will be shared between all targets in the project.
  The most important subfolder is `commonMain`. If preferred, you can add code to the platform-specific folders here
  too.

* `/shared/native` contains code used by C-Interops to build bridges between Kotlin Multiplatform and native libraries

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…

## MusicKit Developer Token

This app uses [MusicKit API](https://developer.apple.com/musickit/), which is an Apple API. This requires a private
key to integrate/authenticate the API calls. For that, you, as a developer, needs
to [create a media identifier and private key](https://developer.apple.com/help/account/configure-app-capabilities/create-a-media-identifier-and-private-key/)
that is used in the code as `PrivateKey` to perform the necessary MusicKit calls.

## Configuring MusicKit Service Key

This project uses [Arkana](https://github.com/rogerluan/arkana) to obfuscate keys and allow injection of
values at compile time. To be able to correct build the project, first:

1. Copy the `.env.sample` file as `.env`
2. Update the values on `.env`
3. Install Arkana with: `gem install arkana`
4. Run Arkana with: `arkana -l kotlin`

## Running iOS/macOS

1. Open the Xcode project
2. Update the Bundle ID and Team ID on [App.xcconfig](App.xcconfig)
3. Run the application

