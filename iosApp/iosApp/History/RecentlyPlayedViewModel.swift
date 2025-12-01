//
//  RecentlyPlayedViewModel.swift
//  iosApp
//
//  Created by Igor Ferreira on 12/3/25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import MusicStream

@Observable
final class RecentlyPlayedViewModel: ListViewModel {
    
    private let recentlyPlayedUseCase: RecentlyPlayedUseCase
    
    private(set) var loading: Bool = false
    private(set) var history: [MusicEntry] = [] {
        didSet { print("New history size: \(history.count)") }
    }
    
    init(useCase: RecentlyPlayedUseCase) {
        self.recentlyPlayedUseCase = useCase
        
        collect(useCase.isPerformingFlow, into: \.loading)
        collect(useCase.resultFlow, into: \.history)
    }
    
    func load() async throws {
        try await self.recentlyPlayedUseCase.perform()
    }
}
