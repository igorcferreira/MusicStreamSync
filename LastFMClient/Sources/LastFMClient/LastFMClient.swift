// The Swift Programming Language
// https://docs.swift.org/swift-book
import Foundation
import AuthenticationServices

public struct LastFMClient: Sendable {
    
    private(set) var keyConfiguration: KeyConfiguration? {
        get {
            KeyConfiguration.restore()
        }
        
        set {
            newValue.store()
        }
    }
    private(set) var userCredentials: UserSession? {
        get {
            UserSession.restore()
        }
        set {
            newValue.store()
        }
    }
    
    private let networkClient: NetworkClient
    
    public var isAuthenticated: Bool {
        return userCredentials?.key != nil
    }
    
    public var userName: String? {
        userCredentials?.name
    }
    
    public init(
        apiKey: String,
        apiSecret: String
    ) {
        self.networkClient = NetworkClient(keyHasher: KeyHasher())
        self.keyConfiguration = .init(apiKey: apiKey, apiSecret: apiSecret)
    }
    
    @concurrent
    public func listLatestTracks(
        user: String? = nil
    ) async throws -> [Track] {
        
        var trackUser = userCredentials?.name
        if let user {
            trackUser = user
        }
        
        let parameters: [String: String] = if let trackUser {
            ["user": trackUser]
        }  else {
            [:]
        }
        
        
        let endpoint = Endpoint(
            apiMethod: "user.getRecentTracks",
            method: .get,
            parameters: parameters
        )
        
        let response: RecenteTracksResponse = try await self.networkClient.perform(endpoint)
        return response.tracks
    }
    
    @concurrent
    public func validate(
        token: String
    ) async throws -> String {
        let endpoint = Endpoint(
            apiMethod: "auth.getSession",
            method: .get,
            parameters: ["token": token]
        )
        let response: SessionResponse = try await self.networkClient.perform(endpoint)
        response.session.store()
        return response.session.name
    }
    
    public func logout() {
        UserSession.erase()
    }
}
