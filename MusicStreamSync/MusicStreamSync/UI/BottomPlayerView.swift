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
