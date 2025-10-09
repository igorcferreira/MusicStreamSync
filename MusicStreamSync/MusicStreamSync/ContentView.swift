//
//  ContentView.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 7/10/25.
//

import SwiftUI
import LastFMClient

struct ContentView: View {
    
    enum TabEntry: String {
        case home = "Home"
        case playlist = "Playlist"
        case scrobble = "Scrobble"
        case search = "Search"
    }
    
    @State private var selectedTab = TabEntry.home
    @Environment(\.lastFMClient) private var lastFMClient
    
    var body: some View {
        TopStyle {
            TabView(selection: $selectedTab) {
                Tab(TabEntry.home.rawValue, systemImage: "music.note.house.fill", value: .home) {
                    HomeView()
                        .navigationTitle(Text(TabEntry.home.rawValue))
                }
                Tab(TabEntry.playlist.rawValue, systemImage: "play.square.stack.fill", value: .playlist) {
                    PlaylistView()
                        .navigationTitle(Text(TabEntry.playlist.rawValue))
                }
                Tab(TabEntry.scrobble.rawValue, systemImage: "icloud.and.arrow.up.fill", value: .scrobble) {
                    Text("Scrobble")
                        .navigationTitle(Text(TabEntry.scrobble.rawValue))
                }
                Tab(TabEntry.search.rawValue, systemImage: "magnifyingglass", value: .search, role: .search) {
                    SearchView()
                        .navigationTitle(Text(TabEntry.search.rawValue))
                }
            }
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    AuthenticationButton(client: lastFMClient)
                }
            }
            #if os(iOS)
            .tabBarMinimizeBehavior(.onScrollDown)
            #endif
            .withBottomPlayer()
        }
#if os(macOS)
        .onAppear {
            NSApplication.shared.setActivationPolicy(.regular)
        }
        .onDisappear {
            NSApplication.shared.setActivationPolicy(.prohibited)
        }
#endif
    }
    
    @ViewBuilder
    func TopStyle<Content: View>(
        @ViewBuilder content: () -> Content
    ) -> some View {
        #if os(macOS)
        content()
            .tabViewStyle(.sidebarAdaptable)
        #else
        NavigationView {
            content()
                .navigationTitle(Text(selectedTab.rawValue))
        }
        #endif
    }
}

#if os(macOS)
extension View {
    @ViewBuilder
    func tabViewBottomAccessory<Content>(
        @ViewBuilder content: () -> Content
    ) -> some View where Content : View {
        overlay(alignment: .bottom, content: content)
    }
}
#endif

#Preview {
    ContentView()
}
