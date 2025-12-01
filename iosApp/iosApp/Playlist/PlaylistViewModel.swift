//
//  PlaylistViewModel.swift
//  iosApp
//
//  Created by Igor Ferreira on 17/03/2025.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI
import MusicStream

@Observable
class PlaylistViewModel: ListViewModel {
    let useCase: PlaylistsUseCase
    
    private(set) var loading: Bool = false
    private(set) var history: [PlaylistEntry] = []
    
    init(useCase: PlaylistsUseCase) {
        self.useCase = useCase
        
        collect(useCase.isPerformingFlow, into: \.loading)
        collect(useCase.resultFlow, into: \.history)
    }
    
    func load() async throws {
        try await useCase.perform()
    }
}

extension PlaylistEntry: @retroactive Identifiable {}
