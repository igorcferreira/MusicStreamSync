//
//  PlaylistViewModel.swift
//  iosApp
//
//  Created by Igor Ferreira on 17/03/2025.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI
import MusicStream

class PlaylistViewModel: ObservableObject, ListViewModel {
    let useCase: PlaylistsUseCase
    
    @Published private(set) var loading: Bool = false
    @Published private(set) var history: [PlaylistEntry] = []
    
    init(useCase: PlaylistsUseCase) {
        self.useCase = useCase
        
        useCase.isPerforming
            .collect(into: \.loading, observer: self)
        useCase.result
            .collect(into: \.history, observer: self)
    }
    
    func load() async throws {
        try await useCase.perform()
    }
}

extension PlaylistEntry: @retroactive Identifiable {}
