# MusicStreamSync

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
4. Run Arkana with: `arkana`

## Running iOS/macOS

1. Open the Xcode workspace
2. Run the application

