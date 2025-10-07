//
//  KeyConfiguration.swift
//  LastFMClient
//
//  Created by Igor Ferreira on 7/10/25.
//
import Foundation

struct KeyConfiguration: Codable, SecureValue {
    static let valueKey: String = "last_fm_api_key"
    
    let apiKey: String
    let apiSecret: String
}
