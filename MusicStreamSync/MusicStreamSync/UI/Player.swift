//
//  Player.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 8/10/25.
//
import SwiftUI

struct PlayerView: View {
    @State private var playerBridge: PlayerBridge
    
    var isPlaying: Bool { playerBridge.isPlaying }
    var currentItem: PlayingItem? { playerBridge.currentItem }
    var label: String {
        isPlaying ? "Playing" : "Paused"
    }
    
    init(playerBridge: PlayerBridge) {
        self.playerBridge = playerBridge
    }
    
    var body: some View {
        Text("\(label) \(currentItem?.title ?? "-")")
    }
}

#Preview("No item available") {
    PlayerView(playerBridge: MockedPlayerBridge(isPlaying: false))
        .frame(height: 54.0)
        .frame(maxWidth: .infinity)
        .glassEffect()
        .padding()
}

#Preview("Playing") {
    PlayerView(playerBridge: MockedPlayerBridge(
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
    ))
    .frame(height: 54.0)
    .frame(maxWidth: .infinity)
    .glassEffect()
    .padding()
}
