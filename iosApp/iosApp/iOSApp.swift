import SwiftUI
import MusicStream
import MusicKit
import BackgroundTasks
import OSLog

@main
struct iOSApp: App {
    private let logger = Logger(subsystem: "dev.igorcferreira.musicstream", category: "music")
    
    @Environment(\.factory) var factory
    @Environment(\.scenePhase) private var phase
    @ObservedObject private var recentlyPlayedViewModel = RecentlyPlayedViewModel(useCase: FactoryKey.defaultValue.makeRecentlyPlayedUseCase())
    @ObservedObject private var playerViewModel = PlayerViewModel(factory: FactoryKey.defaultValue)
    @ObservedObject private var lastFMViewModel = LastFMViewModel()
    
    var body: some Scene {
        WindowGroup(id: "main") {
            VStack {
                PlayerView(viewModel: playerViewModel)
                TabView {
                    ContentView(
                        title: String(localized: "Recently Played"),
                        viewModel: recentlyPlayedViewModel
                    ).tabItem  {
                        Label("History", systemImage: "music.note.house.fill")
                    }
                    ContentView(
                        title: String(localized: "Playlists"),
                        viewModel: PlaylistViewModel(useCase: factory.makePlaylistsUseCase())
                    ).tabItem  {
                        Label("Playlists", systemImage: "play.square.stack.fill")
                    }
                    ScrobbleView(
                        recentlyPlayed: recentlyPlayedViewModel,
                        lastFMViewModel: lastFMViewModel
                    ).tabItem {
                        Label("Scrobble", systemImage: "icloud.and.arrow.up.fill")
                    }
                }
                .toolbar {
                    ToolbarItem(id: "lastfm") {
                        LastFMToolbarItem(lastFMViewModel: lastFMViewModel)
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
