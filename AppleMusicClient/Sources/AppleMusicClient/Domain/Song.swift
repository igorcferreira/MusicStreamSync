//
//  Song.swift
//  AppleMusicClient
//
//  Created by Igor Ferreira on 9/10/25.
//
import Foundation
import MusicKit

extension Song {
    func toPlayerItem() -> PlayingItem {
        .init(
            id: id.rawValue,
            title: title,
            artist: artistName,
            duration: duration ?? 0.0,
            album: albumTitle ?? "",
            url: url,
            artwork: artwork?.data.map({ .local(data: $0) })
        )
    }
}
