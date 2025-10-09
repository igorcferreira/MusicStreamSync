//
//  TokenSigner.swift
//  AppleMusicClient
//
//  Created by Igor Ferreira on 9/10/25.
//

import Foundation
import CryptoKit
import KeychainSwift

protocol TokenSigner: Sendable {
    func sign(_ token: DeveloperToken) throws -> String
}

struct JWTTokenSigner: TokenSigner {
    private static let kKeyName = "JWTTokenSigner.Key"
    
    var privateKey: String? {
        KeychainSwift().get(Self.kKeyName)
    }
    
    init(privateKey: String) {
        KeychainSwift().set(privateKey, forKey: Self.kKeyName)
    }
    
    func sign(_ token: DeveloperToken) throws -> String {
        let unsigned = try token.buildUnsignedToken()
        guard let key = privateKey else {
            throw URLError(.cannotDecodeContentData)
        }
        return try sign(unsigned, privateKey: key)
    }
    
    func sign(_ content: String, privateKey: String) throws -> String {
        guard let data = content.data(using: .utf8) else {
            throw URLError(.cannotDecodeContentData)
        }
        
        do {
            let key = try P256.Signing.PrivateKey(pemRepresentation: privateKey.cleanKey)
            let vault = try key.signature(for: data)
            return vault.rawRepresentation.base64EncodedString()
        } catch {
            throw URLError(.cannotDecodeContentData)
        }
    }
}

private extension String {
    var cleanKey: String {
        guard let rawData = Data(base64Encoded: self), let cleanContent = String(data: rawData, encoding: .utf8) else {
            return self
        }
        return cleanContent
    }
}
