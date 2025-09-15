import SwiftUI
import MusicStream
import MusicKit
import BackgroundTasks
import OSLog
import StoreKit

@main
struct iOSApp: App {
    private let logger = Logger(subsystem: "dev.igorcferreira.musicstream", category: "music")
    
    var appVersion: String? {
        guard let shortVersion = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String else {
            return nil
        }
        
        return if let buildVersion = Bundle.main.infoDictionary?["CFBundleVersion"] as? String {
            "\(shortVersion) (\(buildVersion))"
        } else {
            shortVersion
        }
    }
    
    @Environment(\.factory) var factory
    @Environment(\.scenePhase) private var phase
    @ObservedObject private var recentlyPlayedViewModel = RecentlyPlayedViewModel(useCase: FactoryKey.defaultValue.makeRecentlyPlayedUseCase())
    @ObservedObject private var playerViewModel = PlayerViewModel(factory: FactoryKey.defaultValue)
    @ObservedObject private var lastFMViewModel = LastFMViewModel()
    @State private var storeEnvironment: String? = nil
    
    @ViewBuilder
    var versionHeader: some View {
        HStack {
            if let version = appVersion {
                Text(version)
            }
            if let storeEnvironment = storeEnvironment {
                Text(storeEnvironment)
            }
        }
    }
    
    var body: some Scene {
        WindowGroup(id: "main") {
            VStack {
                versionHeader
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
                .task { await loadEnvironment() }
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
    
    func loadEnvironment() async {
        do {
            let state = try await AppTransaction.shared
            let environment: AppStore.Environment? = if case .verified(let transaction) = state {
                transaction.environment
            } else if case .unverified(let transaction, _) = state {
                transaction.environment
            } else {
                nil
            }
            
            
            guard let environment else { return }
            
            storeEnvironment =  switch environment {
            case .production: "Production"
            case .sandbox: "Sandbox"
            case .xcode: "Xcode"
            default: "Unknown"
            }
        } catch {
            print("Transaction error: \(error)")
            storeEnvironment = nil
        }
    }
}
