//
//  LastFMToolbarItem.swift
//  iosApp
//
//  Created by Igor Ferreira on 22/03/2025.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI

struct LastFMToolbarItem: View {
    @ObservedObject private var lastFMViewModel = LastFMViewModel()
    @State private var authenticating: Bool = false
    
    var body: some View {
        ZStack {
            if lastFMViewModel.isAuthenticated {
                Button("Sign off") { lastFMViewModel.logout() }
            } else {
                Button("Authenticate") { authenticating = true }
            }
        }
        .sheet(isPresented: $authenticating) {
            LastFMAuthentication() {
                await lastFMViewModel.authenticate(username: $0, password: $1)
                authenticating = false
            }
        }
    }
}
