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
