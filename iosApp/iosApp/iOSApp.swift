import SwiftUI
import MusicStream
import MusicKit

@main
struct iOSApp: App {
    @Environment(\.factory) var factory
    
    var body: some Scene {
        WindowGroup(id: "main") {
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
                .toolbar {
                    ToolbarItem(id: "lastfm") {
                        LastFMToolbarItem()
                    }
                }
                .tabViewStyle(.sidebarAdaptable)
#if os(macOS)
                .onAppear {
                    NSApplication.shared.setActivationPolicy(.regular)
                }
                .onDisappear {
                    NSApplication.shared.setActivationPolicy(.prohibited)
                }
#endif
            }
        }
#if os(macOS)
        MenuBarExtra(isInserted: .constant(true)) {
            SyncMenuBar()
        } label: {
            Image(systemName: "music.note.house.fill")
        }
        .menuBarExtraStyle(.window)
#endif
    }
}
