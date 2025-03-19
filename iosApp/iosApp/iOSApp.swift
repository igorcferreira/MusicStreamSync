import SwiftUI
import MusicStream
import ArkanaKeys
import MusicKit
import ScriptingBridge

struct Player {
    let application = SBApplication(bundleIdentifier: "com.apple.music")
    
    
}

@main
struct iOSApp: App {
    @Environment(\.factory) var factory
    
    var body: some Scene {
        WindowGroup {
            VStack {
                PlayerView(factory: factory)
                TabView {
                    ContentView(
                        title: String(localized: "Recently Played"),
                        viewModel: RecentlyPlayedViewModel(useCase: factory.makeRecentlyPlayedUseCase())
                    ).tabItem  {
                        Label("History", systemImage: "music.note.house.fill")
                    }
                    ContentView(
                        title: String(localized: "Playlists"),
                        viewModel: PlaylistViewModel(useCase: factory.makePlaylistsUseCase())
                    ).tabItem  {
                        Label("Playlists", systemImage: "play.square.stack.fill")
                    }
                }
                .tabViewStyle(.sidebarAdaptable)
            }
        }
    }
}
