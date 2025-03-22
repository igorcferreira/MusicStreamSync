import Foundation
import MusicKit

extension MusicAuthorization.Status {
    var isAuthorized: Bool {
        switch self {
        case .authorized, .restricted: return true
        default: return false
        }
    }

    var isDenied: Bool {
        return self == .denied
    }
}

@objc public class MusicKitBridge : NSObject {

    @objc public func authenticate() async throws {
        let current = MusicAuthorization.currentStatus

        guard !current.isAuthorized else { return }
        guard !current.isDenied else {
            throw MusicKit.MusicTokenRequestError.permissionDenied
        }

        let requested = await MusicAuthorization.request()
        if !requested.isAuthorized {
            throw MusicKit.MusicTokenRequestError.permissionDenied
        }
    }

    @objc public func getUserToken(developerToken: String) async throws -> String {
        try await self.authenticate()
        let provider = MusicUserTokenProvider()
        return try await provider.userToken(for: developerToken, options: .ignoreCache)
    }
}