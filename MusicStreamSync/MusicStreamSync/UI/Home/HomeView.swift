//
//  HomeView.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 07/10/2025.
//
import SwiftUI
import LastFMClient

struct HomeView: View {
    private let client: LastFMClient
    
    init(client: LastFMClient) {
        self.client = client
    }
    
    var body: some View {
        Text("Hello")
    }
}

#Preview {
    HomeView(client: LastFMClientKey.defaultValue)
}
