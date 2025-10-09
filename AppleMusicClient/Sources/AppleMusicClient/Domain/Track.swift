//
//  Track.swift
//  AppleMusicClient
//
//  Created by Igor Ferreira on 9/10/25.
//
import Foundation
import MusicKit

extension Artwork {
    var url: URL? {
        let width = max(40, maximumWidth)
        let height = max(40, maximumHeight)
        return url(width: width, height: height)
    }
}

extension Track {
    func toPlayerItem() -> PlayingItem {
        .init(
            id: id.rawValue,
            title: title,
            artist: artistName,
            duration: duration ?? 0.0,
            album: albumTitle ?? "",
            url: url,
            artwork: artworkURL
        )
    }
    
    var artworkURL: URL? {
        artwork?.url
    }
}
