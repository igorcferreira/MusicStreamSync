//
//  BottomPlayerView.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 8/10/25.
//
import SwiftUI
import Combine

struct BottomPlayerView: ViewModifier {
    @State private var playerBridge: PlayerBridge
    
    init(playerBridge: PlayerBridge) {
        self.playerBridge = playerBridge
    }
    
    func body(content: Content) -> some View {
        if playerBridge.currentItem == nil {
            content
        } else {
            content.tabViewBottomAccessory {
                PlayerView(playerBridge: playerBridge)
            }
        }
    }
}

extension View {
    @ViewBuilder
    func withBottomPlayer() -> some View {
        modifier(BottomPlayerView(playerBridge: fetchSystemBridge()))
    }
}

#Preview("No item available") {
    TabView {
        Tab("Hello", systemImage: "house.fill") {
            Text("Hello")
        }
    }
    .modifier(BottomPlayerView(playerBridge: MockedPlayerBridge(isPlaying: false)))
}

#Preview("Playing") {
    TabView {
        Tab("Hello", systemImage: "house.fill") {
            Text("Hello")
        }
    }
    .modifier(BottomPlayerView(playerBridge: MockedPlayerBridge(
        isPlaying: true,
        currentItem: .init(
            id: UUID().uuidString,
            title: "Random song",
            artist: "Random artist",
            duration: 42.0,
            elapsedTime: 2.0,
            album: "Random album",
            artwork: nil
        )
    )))
}
