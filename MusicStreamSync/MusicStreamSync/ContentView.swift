//
//  ContentView.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 7/10/25.
//

import SwiftUI
import ArkanaKeys
import LastFMClient

struct ContentView: View {
    @State var tracks: [Track] = []
    @State var error: Error? = nil
    
    var body: some View {
        VStack {
            if let error {
                Text("Error: \(error.localizedDescription)")
            }
            
            List(tracks) { track in
                Text(track.name)
            }
        }
        .task {
            await loadContent()
        }
        .padding()
    }
    
    @concurrent
    func loadContent() async {
        let arkana = ArkanaKeys.Global()
        let client = LastFMClient(apiKey: arkana.lastFMAPIKey, apiSecret: arkana.lastFMAPISecret)
        do {
            let tracks = try await client.listLatestTracks(user: "igorcferreira")
            await update(tracks)
            await update(error: nil)
        } catch {
            await update(error: error)
        }
    }
    
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
