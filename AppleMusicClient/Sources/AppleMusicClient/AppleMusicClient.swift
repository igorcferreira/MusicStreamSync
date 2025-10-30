// The Swift Programming Language
// https://docs.swift.org/swift-book
import SwiftUI
import MusicKit

public final class AppleMusicClient: Sendable {
    public enum CredentialError: Error {
        case missingLicense
    }
    
    public static let authenticationChange = Notification.Name("AppleMusicClient.authenticationChange")
    public var authorized: Bool {
        MusicAuthorization.currentStatus.authorized
    }
    
    private let token: DeveloperToken?
    private let tokenSigner: TokenSigner?
    
    public init() {
        self.token = nil
        self.tokenSigner = nil
    }
    
    public init(
        teamId: String,
        keyId: String,
        privateKey: String
    ) {
        self.token = .init(teamId: teamId, keyId: keyId)
        self.tokenSigner = JWTTokenSigner(privateKey: privateKey)
    }
    
    @discardableResult
    public func authorize() async -> Bool {
        let old = authorized
        let status = await MusicAuthorization.request()
        let authorization = status.authorized
        if old != authorized {
            await notifyAuthenticationChange()
        }
        return authorization
    }
    
    @concurrent
    public func fetchLatestSongs() async -> [PlayingItem] {
        let request = MusicRecentlyPlayedRequest<Track>()
        do {
            let response = try await request.response()
            return response.items.map { $0.toPlayerItem() }
        } catch {
            return []
        }
    }
    
    @concurrent
    public func fetchPlayslists() async -> [PlayingItem] {
        let request = MusicLibraryRequest<Playlist>()
        do {
            let response = try await request.response()
            return response.items.map { $0.toPlayerItem() }
        } catch {
            return []
        }
    }
    
    @concurrent
    public func searchSong(
        term: String,
        limit: Int = 20,
        offset: Int = 0
    ) async -> [PlayingItem] {
        var request = MusicCatalogSearchRequest(term: term, types: [
            Song.self
        ])
        request.limit = limit
        request.offset = offset
        request.includeTopResults = true
        
        do {
            let response = try await request.response()
            return response.songs.map { $0.toPlayerItem() }
        } catch {
            return []
        }
    }

    @MainActor
    public func getDeveloperToken() async throws -> String {
        guard let token, let tokenSigner else {
            throw CredentialError.missingLicense
        }
        
        let provider = MusicUserTokenProvider()
        let signature = try tokenSigner.sign(token)
        return try await provider.userToken(for: signature, options: .ignoreCache)
    }
    
    @MainActor
    private func notifyAuthenticationChange() {
        NotificationCenter.default.post(
            name: Self.authenticationChange,
            object: authorized
        )
    }
}

extension MusicAuthorization.Status {
    var authorized: Bool {
        switch self {
        case .authorized: true
        case .denied: false
        case .notDetermined: false
        case .restricted: false
        default: false
        }
    }
}

#if DEBUG
import Playgrounds
#Playground {
    let client = AppleMusicClient()
    _ = await client.fetchLatestSongs()
}
#endif
