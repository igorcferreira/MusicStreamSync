//
//  Factory.swift
//  iosApp
//
//  Created by Igor Ferreira on 14/03/2025.
//  Copyright © 2025 orgName. All rights reserved.
//
import Shared
import SwiftUI
import MusicKit

class Factory {
    private(set) lazy var configuration: Shared.Configuration = {
        .init(developerToken: SecureDeveloperToken())
    }()
    
    func makeRecentlyPlayedUseCase() -> RecentlyPlayedUseCase {
        .init(configuration: configuration)
    }
    
    func makePlayerUseCase() -> PlayerUseCase {
        .init()
    }
}

class FactoryKey: EnvironmentKey {
    static let defaultValue: Factory = Factory()
}

extension EnvironmentValues {
    var factory: Factory {
        get { self[FactoryKey.self] }
        set { self[FactoryKey.self] = newValue }
    }
}
