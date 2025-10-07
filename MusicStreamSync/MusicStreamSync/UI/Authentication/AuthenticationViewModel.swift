//
//  AuthenticationViewModel.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 07/10/2025.
//
import SwiftUI
import LastFMClient

@Observable
class AuthenticationViewModel: Sendable {
    private(set) var isAuthenticated: Bool = false
    var showAuthentication: Bool = false
    
    private let client: LastFMClient
    
    init(client: LastFMClient) {
        self.client = client
        handleAuthentication()
    }
    
    @ViewBuilder
    func loginView() -> some View {
        client.loginView { _ in
            await self.handleAuthentication()
        }
    }
    
    func logout() {
        client.logout()
        handleAuthentication()
    }
    
    func login() {
        showAuthentication = true
    }
    
    @MainActor
    func handleAuthentication() {
        showAuthentication = false
        isAuthenticated = client.isAuthenticated
    }
}
