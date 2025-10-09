//
//  DeveloperToken.swift
//  AppleMusicClient
//
//  Created by Igor Ferreira on 9/10/25.
//
import Foundation
import KeychainSwift

struct DeveloperToken: Sendable {
    let teamId: String
    let keyId: String
}

extension DeveloperToken {
    func buildUnsignedToken() throws -> String {
        let date = Date.now
        let calendar = Calendar.current
        
        let header = [
            "alg": "ES256",
            "kid": keyId
        ]
        
        let payload: [String: Any] = [
            "iss": teamId,
            "iat": Int(date.timeIntervalSince1970),
            "exp": Int(calendar.date(byAdding: .day, value: 1, to: .now)!.timeIntervalSince1970)
        ]
        
        let line = [
            try JSONSerialization.data(withJSONObject: header),
            try JSONSerialization.data(withJSONObject: payload)
        ].map({ $0.base64EncodedString() }).joined(separator: ".")
        
        return line
    }
}
