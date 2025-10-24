//
//  Playlist.swift
//  AppleMusicClient
//
//  Created by Igor Ferreira on 9/10/25.
//
import MusicKit

extension Playlist {
    func toPlayerItem() -> PlayingItem {
        .init(
            id: id.rawValue,
            title: name,
            artist: curatorName ?? "",
            duration: 0.0,
            album: "",
            url: url,
            artwork: artwork?.data.map({ .local(data: $0) })
        )
    }
}
