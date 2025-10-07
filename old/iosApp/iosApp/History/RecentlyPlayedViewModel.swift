//
//  RecentlyPlayedViewModel.swift
//  iosApp
//
//  Created by Igor Ferreira on 12/3/25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import MusicStream

final class RecentlyPlayedViewModel: ObservableObject, ListViewModel {
    
    private let recentlyPlayedUseCase: RecentlyPlayedUseCase
    
    @Published private(set) var loading: Bool = false
    @Published private(set) var history: [MusicEntry] = [] {
        didSet { print("New history size: \(history.count)") }
    }
    
    init(useCase: RecentlyPlayedUseCase) {
        self.recentlyPlayedUseCase = useCase
        
        useCase.isPerforming
            .collect(into: \.loading, observer: self)
        useCase.result.sinkOnMain { [weak self] (buffer: [MusicEntry]) in
            if (!buffer.isEmpty) { self?.history = buffer }
        }
    }
    
    func load() async throws {
        try await self.recentlyPlayedUseCase.perform()
    }
}
