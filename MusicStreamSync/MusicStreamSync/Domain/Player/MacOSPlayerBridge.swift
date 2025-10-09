//
//  MacOSPlayerBridge.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 8/10/25.
//
#if os(macOS)
import Foundation
import MediaRemote
import AppleMusicClient
import Combine

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
                let current = await self.getCurrentItem()
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
    
    func play(_ item: PlayingItem) async {
        player.resume()
    }
    
    func play(_ items: [PlayingItem]) async {
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
            album: item.album ?? "",
            url: nil,
            artwork: nil
        )
    }
    
    @MainActor
    private func update(currentItem: PlayingItem?) {
        self.isPlaying = currentItem != nil
        self.currentItem = currentItem
    }
}
#endif

