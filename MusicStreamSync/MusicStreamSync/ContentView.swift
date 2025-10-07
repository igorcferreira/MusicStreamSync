//
//  ContentView.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 7/10/25.
//

import SwiftUI
import LastFMClient

struct ContentView: View {
    @State private var tracks: [Track] = []
    @State private var error: Error? = nil
    @State private var showAuthentication: Bool = false
    @State private var isAuthenticated: Bool = false
    @Environment(\.lastFMClient) private var client
    
    var body: some View {
        VStack {
            
            if isAuthenticated {
                Button("Logout") {
                    client.logout()
                    handleAuthentication()
                }
            } else {
                Button("Authenticate") {
                    showAuthentication = true
                }
            }
            
            if let error {
                Text("Error: \(error.localizedDescription)")
            }
            
            List(tracks) { track in
                VStack(alignment: .leading) {
                    Text(track.name)
                        .font(.body)
                    Text(track.album.text)
                        .font(.footnote)
                }
                .id(track.id)
            }
        }
        .task {
            handleAuthentication()
        }
        .padding()
        .sheet(isPresented: $showAuthentication) {
            LoginView()
        }
    }
    
    @ViewBuilder
    func LoginView() -> some View {
        NavigationView {
            client.loginView { _ in
                await handleAuthentication()
            }
            .navigationTitle("Login")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItemGroup(placement: .primaryAction) {
                    Button {
                        handleAuthentication()
                    } label: {
                        Image(systemName: "xmark")
                    }
                    .buttonStyle(.glassProminent)
                }
            }
        }
    }
    
    @MainActor
    func handleAuthentication() {
        self.showAuthentication = false
        isAuthenticated = client.isAuthenticated
        if isAuthenticated {
            loadContent()
        }
    }
    
    func loadContent() { Task.detached {
        do {
            let tracks = try await client.listLatestTracks()
            await update(tracks)
            await update(error: nil)
        } catch {
            await update(error: error)
        }
    }}
    
    func update(error: Error?) {
        self.error = error
    }
    
    func update(_ tracks: [Track]) {
        self.tracks = tracks
    }
}

#Preview {
    ContentView()
}
