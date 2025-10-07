//
//  CorrectableText.swift
//  LastFMClient
//
//  Created by Igor Ferreira on 7/10/25.
//
import Foundation

public struct CorrectableText: Codable, Sendable {
    public enum CodingKeys: String, CodingKey {
        case corrected
        case text = "#text"
    }
    
    public let corrected: String?
    public let text: String
}
