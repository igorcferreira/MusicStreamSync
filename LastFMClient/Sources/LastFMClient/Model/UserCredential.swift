//
//  UserCredential.swift
//  LastFMClient
//
//  Created by Igor Ferreira on 7/10/25.
//
import Foundation

struct UserCredential: Sendable, Codable, SecureValue {
    static let valueKey: String = "last_fm_user_credential"
    
    let name: String
    let token: String
}
