import SwiftUI
import MusicStream
import MusicKit
import BackgroundTasks
import OSLog

@main
struct iOSApp: App {
    private let kBackgroundTask = "dev.igorcferreira.musicstream.current"
    private let logger = Logger(subsystem: "dev.igorcferreira.musicstream", category: "music")
    
    @Environment(\.factory) var factory
    @Environment(\.scenePhase) private var phase
    @ObservedObject private var playerViewModel = PlayerViewModel(factory: FactoryKey.defaultValue)
    @ObservedObject private var lastFMViewModel = LastFMViewModel()
    
    var body: some Scene {
        WindowGroup(id: "main") {
            VStack {
                PlayerView(viewModel: playerViewModel)
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
        .onChange(of: phase) { _, newPhase in
            if case .background = newPhase {
                requestBackgroundTask()
            }
        }
#if os(iOS)
        .backgroundTask(.appRefresh(kBackgroundTask)) {
            try? await Task.sleep(for: .seconds(2))
            let isPlaying = await playerViewModel.isPlaying
            logger.info("Background Task -  Is playing: \(isPlaying)")
            await requestBackgroundTask()
        }
#endif
#if os(macOS)
        MenuBarExtra(isInserted: .constant(true)) {
            SyncMenuBar()
        } label: {
            Image(systemName: "music.note.house.fill")
        }
        .menuBarExtraStyle(.window)
#endif
    }
    
    func requestBackgroundTask() {
#if os(iOS)
        let request = BGAppRefreshTaskRequest(identifier: kBackgroundTask)
        request.earliestBeginDate = Date().addingTimeInterval(60)
        do {
            try BGTaskScheduler.shared.submit(request)
            logger.info("Requested background task")
        } catch {
            logger.info("Failed to request background task: \(error)")
        }
#endif
    }
}
