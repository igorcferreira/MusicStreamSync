//
//  PlayerViewModel.swift
//  iosApp
//
//  Created by Igor Ferreira on 15/03/2025.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI
import Shared

class PlayerViewModel: ObservableObject {
    let playerUseCase: PlayerUseCase
    @Published private(set) var isPlaying: Bool = false
    @Published private(set) var playingItem: SongEntry?
    
    init(playerUseCase: PlayerUseCase) {
        self.playerUseCase = playerUseCase
        self.playerUseCase.isPlaying
            .collect(into: \.isPlaying, observer: self)
        self.playerUseCase.playingItem
            .collect(into: \.playingItem, observer: self)
    }
    
    convenience init(factory: Factory) {
        self.init(playerUseCase: factory.makePlayerUseCase())
    }
    
    func play() { playerUseCase.play() }
    func pause() { playerUseCase.pause() }
}

