//
//  Track.swift
//  LastFMClient
//
//  Created by Igor Ferreira on 7/10/25.
//
import Foundation

public struct Track: Identifiable, Codable, Sendable {
    
    enum CodingKeys: String, CodingKey {
        case name
        case artist
        case uts = "date"
        case album
    }
    
    public struct TrackDate: Codable, Sendable {
        let uts: String
        var timeinterval: TimeInterval {
            guard let time = Int(uts) else {
                return 0.0
            }
            return Double(time)
        }
    }
    
    public let id: UUID = UUID()
    public let name: String
    public let artist: CorrectableText
    public let uts: TrackDate
    public let album: CorrectableText

    public var date: Date {
        Date(timeIntervalSince1970: Double(uts.timeinterval))
    }
}
