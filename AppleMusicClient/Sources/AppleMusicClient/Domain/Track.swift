//
//  Track.swift
//  AppleMusicClient
//
//  Created by Igor Ferreira on 9/10/25.
//
import Foundation
import MusicKit

extension Track {
    func toPlayerItem() -> PlayingItem {
        .init(
            id: id.rawValue,
            title: title,
            artist: artistName,
            duration: duration ?? 0.0,
            album: albumTitle ?? "",
            url: url,
            artwork: artworkURL.map({ .remote(url: $0) })
        )
    }
}


extension Track: MediaItemArtwork {}
