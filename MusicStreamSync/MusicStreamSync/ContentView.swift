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
    @Environment(\.lastFMClient) private var client
    
    var body: some View {
        VStack {
            AuthenticationButton()
            
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
        .onReceive(NotificationCenter.default.publisher(for: .LastFMAuthenticationChanged)) { _ in
            loadContent()
        }
        .task {
            loadContent()
        }
    }
    
    func loadContent() { Task.detached {
        guard await client.isAuthenticated else {
            return
        }
        
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
