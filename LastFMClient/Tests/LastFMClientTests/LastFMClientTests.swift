//
//  KeyHasherTests.swift
//  LastFMClient
//
//  Created by Igor Ferreira on 7/10/25.
//
import Testing
@testable import LastFMClient

@Suite("Key Hasher Tests")
struct KeyHasherTests {
    @Test
    func testHash() async throws {
        let hasher = KeyHasher()
        let secret = "api-secret"
        let parameters = [
            "method": "track.updateNowPlaying",
            "artist": "Artist",
            "track": "Track",
            "album": "Album"
        ]
        
        let value = try hasher.hash(secret: secret, parameter: parameters)
        #expect("2ea21f67c97af5c5370abe183c9874bf" == value)
    }
}
