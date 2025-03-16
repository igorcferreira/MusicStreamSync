import CryptoKit
import Foundation

@objc public class KCrypto : NSObject {
    @objc public func load(pemKey: Data) throws -> String {
        guard let content = String(data: pemKey, encoding: .utf8) else {
            throw URLError(.cannotDecodeContentData)
        }
        return content
    }

    @objc public func sign(_ content: String, pemKey: String) throws -> String {
        guard let data = content.data(using: .utf8) else {
            throw URLError(.cannotDecodeContentData)
        }

        do {
            let key = try P256.Signing.PrivateKey(pemRepresentation: pemKey)
            let vault = try key.signature(for: data)
            return vault.rawRepresentation.base64EncodedString()
        } catch {
            throw URLError(.cannotDecodeContentData)
        }
    }
}
