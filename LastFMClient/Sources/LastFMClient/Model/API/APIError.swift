//
//  APIError.swift
//  LastFMClient
//
//  Created by Igor Ferreira on 7/10/25.
//
import Foundation

public struct APIError: Codable, Sendable, LocalizedError {
    public let message: String
    public let error: Int
    
    public var errorDescription: String? {
        message
    }
}
