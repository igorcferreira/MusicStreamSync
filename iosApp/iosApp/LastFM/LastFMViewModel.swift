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
    
    func logout() {
        useCase.logout()
        isAuthenticated = false
    }
}
