//
//  LastFMViewModel.swift
//  iosApp
//
//  Created by Igor Ferreira on 22/03/2025.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI
import MusicStream

class LastFMViewModel: ObservableObject {
    private let useCase: LastFMUseCase
    
    @Published var isAuthenticated: Bool = false
    
    init(useCase: LastFMUseCase = LastFMUseCase()) {
        self.useCase = useCase
        useCase.isAuthenticated
            .collect(into: \.isAuthenticated, observer: self)
    }
    
    func authenticate(
        username: String,
        password: String
    ) async {
        do {
            try await useCase.authenticate(username: username, password: password)
            Task { @MainActor in isAuthenticated = true }
        } catch {
            Task { @MainActor in isAuthenticated = false }
        }
    }
    
    func scrobble(_ selection: [MusicEntry], items: [MusicEntry]) async {
        //This is done to keep the sorting of the input, regardless
        //of the order of the selection made by the user.
        let itemsToUpload = items.filter({ selection.contains($0) })
        try? await useCase.scrobble(selection: itemsToUpload)
    }
    
    func logout() {
        useCase.logout()
        isAuthenticated = false
    }
}
