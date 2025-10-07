// The Swift Programming Language
// https://docs.swift.org/swift-book
import Foundation

public struct LastFMClient: Sendable {
    
    private var keyConfiguration: KeyConfiguration? {
        get {
            KeyConfiguration.restore()
        }
        
        set {
            newValue.store()
        }
    }
    private var userCredentials: UserCredential? {
        get {
            UserCredential.restore()
        }
        set {
            newValue.store()
        }
    }
    
    private let networkClient: NetworkClient
    
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
}
