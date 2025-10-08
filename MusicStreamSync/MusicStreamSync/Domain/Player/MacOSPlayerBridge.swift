//
//  MacOSPlayerBridge.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 8/10/25.
//
#if os(macOS)
import Foundation
import MediaRemote

@Observable
class MacOSPlayerBridge: PlayerBridge, Sendable {
    let player = MSMediaRemote()
    
    private(set) var isPlaying: Bool = false
    private(set) var currentItem: PlayingItem? = nil
    private var stateTimer: AnyCancellable? = nil
    
    init() {
        self.stateTimer = Timer.publish(every: 1.0, on: .current, in: .common)
            .autoconnect()
            .sink(receiveValue: { _ in Task.detached {
                let current = await getCurrentItem()
                await self.update(currentItem: current)
            }})
    }
    
    deinit {
        stateTimer?.cancel()
        stateTimer = nil
    }
    
    func play() async {
        player.resume()
    }
    
    func pause() async {
        player.pause()
    }
    
    func getCurrentItem() async -> PlayingItem? {
        guard let item = await player.currentItem() else {
            return nil
        }
        
        return .init(
            id: item.catalogId,
            title: item.title,
            artist: item.artist,
            duration: item.duration,
            elapsedTime: item.elapsedTime,
            album: item.album ?? "",
            artwork: item.artworkData
        )
    }
    
    @MainActor
    private func update(currentItem: PlayingItem?) {
        self.isPlaying = currentItem != nil
        self.currentItem = currentItem
    }
}
#endif

