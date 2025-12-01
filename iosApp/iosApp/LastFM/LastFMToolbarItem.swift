//
//  LastFMToolbarItem.swift
//  iosApp
//
//  Created by Igor Ferreira on 22/03/2025.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI

struct LastFMToolbarItem: View {
    @State private var lastFMViewModel: LastFMViewModel
    @State private var authenticating: Bool = false
    
    init(lastFMViewModel: LastFMViewModel = LastFMViewModel()) {
        self.lastFMViewModel = lastFMViewModel
    }
    
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
