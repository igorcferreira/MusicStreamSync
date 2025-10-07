//
//  Environment.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 7/10/25.
//
import SwiftUI
import LastFMClient
import ArkanaKeys

struct LastFMClientKey: EnvironmentKey {
    static var defaultValue: LastFMClient = {
        let arkana = ArkanaKeys.Global()
        let client = LastFMClient(apiKey: arkana.lastFMAPIKey, apiSecret: arkana.lastFMAPISecret)
        return client
    }()
}

extension EnvironmentValues {
    var lastFMClient: LastFMClient {
        get {
            self[LastFMClientKey.self]
        }
        set {
            self[LastFMClientKey.self] = newValue
        }
    }
}
