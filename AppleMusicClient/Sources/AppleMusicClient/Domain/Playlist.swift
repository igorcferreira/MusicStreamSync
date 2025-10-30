//
//  Playlist.swift
//  AppleMusicClient
//
//  Created by Igor Ferreira on 9/10/25.
//
import MusicKit
import Foundation

extension Playlist {
    func toPlayerItem() -> PlayingItem {
        .init(
            id: id.rawValue,
            title: name,
            artist: curatorName ?? "",
            duration: 0.0,
            album: "",
            url: url,
            artwork: artworkURL.map({ .remote(url: $0) })
        )
    }
}

extension Playlist: MediaItemArtwork {}
