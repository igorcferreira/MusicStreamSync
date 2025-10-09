//
//  PlaylistView.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 9/10/25.
//
import SwiftUI
import AppleMusicClient
import NukeUI

struct PlaylistView: View {
    @Environment(\.appleMusicClient) var appleMusicClient
    @Environment(\.playerBridge) var playerBridge
    @State private var authorized: Bool = false
    @State private var items: [PlayingItem] = []
    
    var body: some View {
        Group {
            if authorized {
                list
                    .onAppear {
                        loadList()
                    }
            } else {
                Button("Authorize Music") {
                    requestAuthorization()
                }
            }
        }.task {
            authorized = appleMusicClient.authorized
        }
    }
    
    @ViewBuilder
    var list: some View {
        List(items) { item in
            HStack {
                if let artwork = item.artwork {
                    LazyImage(url: artwork) { phase in
                        phase.image?.resizable()
                    }
                    .frame(width: 40, height: 40)
                    .cornerRadius(4.0)
                }
                Text(item.title)
            }
            .onTapGesture {
                play(item: item)
            }
        }
    }
    
    func play(item: PlayingItem) { Task {
        await playerBridge.play(item)
    }}
    
    func requestAuthorization() { Task {
        authorized = await appleMusicClient.authorize()
    }}
    
    func loadList() { Task {
        items = await appleMusicClient.fetchPlayslists()
    }}
}

#Preview {
    PlaylistView()
}
