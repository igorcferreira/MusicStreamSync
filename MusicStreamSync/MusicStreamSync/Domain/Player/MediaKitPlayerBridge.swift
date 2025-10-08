//
//  MediaKitPlayerBridge.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 8/10/25.
//
#if os(iOS)
import Foundation
import MediaPlayer
import Combine

@Observable
class MediaKitPlayerBridge: PlayerBridge {
    private let player = MPMusicPlayerController.systemMusicPlayer
    
    private(set) var isPlaying: Bool = false
    private(set) var currentItem: PlayingItem? = nil
    private var stateTimer: AnyCancellable? = nil
    
    init() {
        self.isPlaying = player.playbackState == .playing
        self.stateTimer = Timer.publish(every: 1.0, on: .current, in: .common)
            .autoconnect()
            .sink(receiveValue: { _ in Task.detached {
                let item = await self.getCurrentItem()
                await self.update(currentItem: item)
            }})
    }
    
    deinit {
        stateTimer?.cancel()
        stateTimer = nil
    }
    
    func play() async {
        player.play()
    }
    
    func pause() async {
        player.pause()
    }
    
    func getCurrentItem() async -> PlayingItem? {
        guard let item = player.nowPlayingItem else {
            return nil
        }
        return .init(
            id: item.playbackStoreID,
            title: item.title ?? "",
            artist: item.artist ?? "",
            duration: item.playbackDuration,
            elapsedTime: item.playbackDuration,
            album: item.albumTitle ?? "",
            artwork: item.artworkData
        )
    }
    
    @MainActor
    private func update(currentItem: PlayingItem?) {
        self.isPlaying = self.player.playbackState == .playing
        self.currentItem = currentItem
    }
}

extension MPMediaItem {
    var artworkData: Data? {
        guard let artwork else {
            return nil
        }
        let size = artwork.bounds.size
        guard let image = artwork.image(at: size) else {
            return nil
        }
        let data = image.jpegData(compressionQuality: 1.0)
        return data
    }
}
#endif
