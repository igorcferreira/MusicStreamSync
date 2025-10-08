//
//  PlayingItem.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 8/10/25.
//
import Foundation

struct PlayingItem: Identifiable, Codable, Sendable, Equatable {
    static func ==(lhs: PlayingItem, rhs: PlayingItem) -> Bool {
        return lhs.id == rhs.id
    }
    
    let id: String
    let title: String
    let artist: String
    let duration: TimeInterval
    let elapsedTime: TimeInterval
    let album: String
    let artwork: Data?
}
