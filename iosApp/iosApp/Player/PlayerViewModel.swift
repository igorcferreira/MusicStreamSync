//
//  PlayerViewModel.swift
//  iosApp
//
//  Created by Igor Ferreira on 15/03/2025.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI
import MusicStream

@Observable
open class PlayerViewModel {
    let playerUseCase: PlayerUseCase?
    fileprivate(set) var isPlaying: Bool = false
    fileprivate(set) var playingItem: MusicEntry?
    
    init(playerUseCase: PlayerUseCase?) {
        self.playerUseCase = playerUseCase
        guard let playerUseCase else { return }
        
        collect(playerUseCase.isPlayingFlow, into: \.isPlaying)
        collect(playerUseCase.playingItemFlow, into: \.playingItem)
    }
    
    convenience init(factory: Factory) {
        self.init(playerUseCase: factory.makePlayerUseCase())
    }
    
    open func play() { playerUseCase?.play() }
    open func pause() { playerUseCase?.pause() }
}

#if DEBUG
class MockedPlayerViewModel: PlayerViewModel {
    init(
        isPlaying: Bool,
        playingItem: MusicEntry?
    ) {
        super.init(playerUseCase: nil)
        self.isPlaying = isPlaying
        self.playingItem = playingItem
    }
    
    override func play() {}
    override func pause() {}
}
#endif

