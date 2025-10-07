//
//  KeyHasher.swift
//  LastFMClient
//
//  Created by Igor Ferreira on 7/10/25.
//
import Foundation
import CryptoKit

struct KeyHasher {
    enum HasherError: Error {
        case unableToHash
    }
    
    func hash(
        secret: String,
        parameter: [String: String]
    ) throws -> String {
        var components = [String]()
        parameter.keys.sorted().forEach { key in
            guard let value = parameter[key] else { return }
            let pair = "\(key)\(value)"
            components.append(pair)
        }
        components.append(secret)
        let base = components.joined()
        guard let baseData = base.data(using: .utf8) else {
            throw HasherError.unableToHash
        }
        
        let digest = Insecure.MD5.hash(data: baseData)
            .map { String(format: "%02x", $0) }
            .joined()
        return digest
    }
}
