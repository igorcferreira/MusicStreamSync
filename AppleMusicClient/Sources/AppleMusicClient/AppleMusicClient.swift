// The Swift Programming Language
// https://docs.swift.org/swift-book
import SwiftUI
import MusicKit

public final class AppleMusicClient: Sendable {
    public var authorized: Bool {
        MusicAuthorization.currentStatus.authorized
    }
    
    public func authorize() async -> Bool {
        let status = await MusicAuthorization.request()
        let authorization = status.authorized
        return authorization
    }
    
    public func fetchLatestSongs() async -> [PlayingItem] {
        let request = MusicRecentlyPlayedRequest<Track>()
        do {
            let response = try await request.response()
            return response.items.map { $0.toPlayerItem() }
        } catch {
            return []
        }
    }
    
    public func fetchPlayslists() async -> [PlayingItem] {
        let request = MusicLibraryRequest<Playlist>()
        do {
            let response = try await request.response()
            return response.items.map { $0.toPlayerItem() }
        } catch {
            return []
        }
    }
}

struct AppleMusicClientKey: EnvironmentKey {
    static let defaultValue: AppleMusicClient = .init()
}

public extension EnvironmentValues {
    var appleMusicClient: AppleMusicClient {
        get {
            self[AppleMusicClientKey.self]
        } set {
            self[AppleMusicClientKey.self] = newValue
        }
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
