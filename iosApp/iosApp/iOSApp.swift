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
    @State private var recentlyPlayedViewModel = RecentlyPlayedViewModel(useCase: FactoryKey.defaultValue.makeRecentlyPlayedUseCase())
    @State private var playerViewModel = PlayerViewModel(factory: FactoryKey.defaultValue)
    @State private var lastFMViewModel = LastFMViewModel()
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
            TabView {
                Tab("History", systemImage: "music.note.house.fill") {
                    NavigationStack {
                        ContentView(
                            title: String(localized: "Recently Played"),
                            viewModel: recentlyPlayedViewModel
                        )
                        .toolbar {
                            ToolbarItem(id: "lastfm") {
                                LastFMToolbarItem(lastFMViewModel: lastFMViewModel)
                            }
                        }
                    }
                }
                
                Tab("Playlists", systemImage: "play.square.stack.fill") {
                    NavigationStack {
                        ContentView(
                            title: String(localized: "Playlists"),
                            viewModel: PlaylistViewModel(useCase: factory.makePlaylistsUseCase())
                        )
                        .toolbar {
                            ToolbarItem(id: "lastfm") {
                                LastFMToolbarItem(lastFMViewModel: lastFMViewModel)
                            }
                        }
                    }
                }
                
                Tab("Scrobble", systemImage: "icloud.and.arrow.up.fill") {
                    NavigationStack {
                        ScrobbleView(
                            recentlyPlayed: recentlyPlayedViewModel,
                            lastFMViewModel: lastFMViewModel
                        )
                        .toolbar {
                            ToolbarItem(id: "lastfm") {
                                LastFMToolbarItem(lastFMViewModel: lastFMViewModel)
                            }
                        }
                    }
                }
            }
            .task { await loadEnvironment() }
            .tabBarMinimizeBehavior(.onScrollDown)
            .connecting(player: playerViewModel)
            .toolbar {
                ToolbarItem(placement: .title) {
                    versionHeader
                }
            }
        }
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

extension View {
    @ViewBuilder
    func connecting(player: PlayerViewModel) -> some View {
        if player.playingItem != nil {
            self.tabViewBottomAccessory {
                PlayerView(viewModel: player)
            }
        } else {
            self
        }
    }
}
