//
//  Factory.swift
//  iosApp
//
//  Created by Igor Ferreira on 14/03/2025.
//  Copyright © 2025 orgName. All rights reserved.
//
import MusicStream
import SwiftUI
import MusicKit
import SwiftJWT

struct JWTClaims: Claims, Codable {
    let exp: Date?
    let iat: Date?
    let iss: String?
}

class JWTTokenSigner: TokenSigner {
    func sign(jwtToken: String, privateKey: String) async throws -> String {
        guard let keyData = Data(base64Encoded: privateKey, options: .ignoreUnknownCharacters) else {
            throw URLError(.init(rawValue: 401))
        }
        do {
            let signer = JWTSigner.es256(privateKey: keyData)
            var jwt = try JWT<JWTClaims>(jwtString: jwtToken)
            let signedJWT = try jwt.sign(using: signer)
            guard let signature = signedJWT.stringArray.last else {
                throw URLError(.init(rawValue: 401))
            }
            
            return signature
        } catch {
            print("Error: \(error)")
            throw error
        }
    }
}

class Factory {
    private(set) lazy var configuration: MusicStream.Configuration = {
        .init(tokenSigner: JWTTokenSigner())
    }()
    
    func makeRecentlyPlayedUseCase() -> RecentlyPlayedUseCase {
        .init(configuration: configuration)
    }
    
    func makePlayerUseCase() -> PlayerUseCase {
        .init()
    }
    
    func makePlaylistsUseCase() -> PlaylistsUseCase {
        .init(configuration: configuration)
    }
}

class FactoryKey: EnvironmentKey {
    static let defaultValue: Factory = Factory()
}

extension EnvironmentValues {
    var factory: Factory {
        get { self[FactoryKey.self] }
        set { self[FactoryKey.self] = newValue }
    }
}
