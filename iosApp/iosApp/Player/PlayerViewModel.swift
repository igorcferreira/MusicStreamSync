//
//  PlayerViewModel.swift
//  iosApp
//
//  Created by Igor Ferreira on 15/03/2025.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI
import MusicStream

open class PlayerViewModel: ObservableObject {
    let playerUseCase: PlayerUseCase?
    @Published open fileprivate(set) var isPlaying: Bool = false
    @Published open fileprivate(set) var playingItem: MusicEntry?
    
    init(playerUseCase: PlayerUseCase?) {
        self.playerUseCase = playerUseCase
        self.playerUseCase?.isPlaying
            .collect(into: \.isPlaying, observer: self)

        self.playerUseCase?.playingItem.sinkOnMain { [weak self] (item: MusicEntry?) in
            guard let playing = item else { return }
            if playing.entryId != self?.playingItem?.entryId {
                self?.playingItem = playing
            }
        }
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

