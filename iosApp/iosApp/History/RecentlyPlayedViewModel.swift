//
//  RecentlyPlayedViewModel.swift
//  iosApp
//
//  Created by Igor Ferreira on 12/3/25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import Shared

final class RecentlyPlayedViewModel: ObservableObject {
    
    private let recentlyPlayedUseCase: RecentlyPlayedUseCase
    
    @Published private(set) var loading: Bool = false
    @Published private(set) var history: [SongEntry] = []
    
    init(useCase: RecentlyPlayedUseCase) {
        self.recentlyPlayedUseCase = useCase
        
        useCase.isPerforming
            .collect(into: \.loading, observer: self)
        useCase.result
            .collect(into: \.history, observer: self)
    }
    
    func updateHistory() async throws {
        try await self.recentlyPlayedUseCase.perform()
    }
}
