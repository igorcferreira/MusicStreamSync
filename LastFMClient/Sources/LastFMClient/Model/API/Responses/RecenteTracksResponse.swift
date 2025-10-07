//
//  RecenteTracksResponse.swift
//  LastFMClient
//
//  Created by Igor Ferreira on 7/10/25.
//
import Foundation

struct RecenteTracksResponse: Codable, Sendable {
    struct Metadata: Codable, Sendable {
        let track: [Track]
    }
    let recenttracks: Metadata
    
    var tracks: [Track] {
        recenttracks.track
    }
}
