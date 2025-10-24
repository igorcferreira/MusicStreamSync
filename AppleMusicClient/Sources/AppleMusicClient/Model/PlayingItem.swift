//
//  PlayingItem.swift
//  AppleMusicClient
//
//  Created by Igor Ferreira on 9/10/25.
//
import Foundation

public struct PlayingItem: Identifiable, Codable, Sendable, Equatable {
    public enum Artwork: Sendable, Codable {
        case remote(url: URL)
        case local(data: Data)
    }
    
    public static func ==(lhs: PlayingItem, rhs: PlayingItem) -> Bool {
        return lhs.id == rhs.id
    }
    
    public let id: String
    public let title: String
    public let artist: String
    public let duration: TimeInterval
    public let album: String
    public let url: URL?
    public let artwork: Artwork?
    
    public init(id: String, title: String, artist: String, duration: TimeInterval, album: String, url: URL?, artwork: Artwork?) {
        self.id = id
        self.title = title
        self.artist = artist
        self.duration = duration
        self.album = album
        self.url = url
        self.artwork = artwork
    }
}
