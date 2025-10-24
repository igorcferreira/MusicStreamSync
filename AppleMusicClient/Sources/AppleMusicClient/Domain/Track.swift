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
        let width = min(max(40, maximumWidth), 400)
        let height = min(max(40, maximumHeight), 400)
        return url(width: width, height: height)
    }
    
    var data: Data? {
        guard let url = url else {
            return nil
        }
        print("Artwork url: \(url)")
        return try? Data(contentsOf: url)
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
            artwork: artwork?.data.map({ .local(data: $0) })
        )
    }
}
