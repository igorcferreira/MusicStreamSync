//
//  SecureValue.swift
//  LastFMClient
//
//  Created by Igor Ferreira on 7/10/25.
//
import Foundation
import KeychainSwift

protocol SecureValue {
    static var valueKey: String { get }
    static func restore() -> Self?
    static func erase()
    func store()
}

extension SecureValue {
    static func erase() {
        let keychain = KeychainSwift()
        keychain.delete(Self.valueKey)
    }
}

extension SecureValue where Self: Encodable {
    func store() {
        let keychain = KeychainSwift()
        
        do {
            let encoder = JSONEncoder()
            let data = try encoder.encode(self)
            keychain.set(data, forKey: Self.valueKey)
        } catch {
            keychain.delete(Self.valueKey)
        }
    }
}

extension SecureValue where Self: Decodable {
    static func restore() -> Self? {
        let keychain = KeychainSwift()
        guard let data = keychain.getData(Self.valueKey) else {
            return nil
        }
        do {
            let decoder = JSONDecoder()
            return try decoder.decode(Self.self, from: data)
        } catch {
            keychain.delete(Self.valueKey)
            return nil
        }
    }
}

extension Optional where Wrapped: SecureValue {
    func store() {
        if let value = self {
            value.store()
        } else {
            Wrapped.erase()
        }
    }
}
