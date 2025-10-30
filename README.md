# MusicStreamSync

## Configuring LastFM API Keys

This project uses [Arkana](https://github.com/rogerluan/arkana) to obfuscate keys and allow injection of
values at compile time. To be able to correct build the project, first:

1. Copy the `.env.sample` file as `.env`
2. Update the values on `.env`
3. Install Arkana with: `gem install arkana`
4. Run Arkana with: `arkana`

## Running iOS/macOS

1. Open the Xcode workspace
2. Run the application

