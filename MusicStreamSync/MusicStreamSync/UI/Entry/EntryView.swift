//
//  EntryView.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 9/10/25.
//
import SwiftUI
import AppleMusicClient
import NukeUI

struct EntryView: View {
    @Environment(\.playerBridge) var playerBridge
    
    let item: PlayingItem
    
    init(item: PlayingItem) {
        self.item = item
    }
    
    var body: some View {
        HStack(spacing: 8.0) {
            if let artwork = item.artwork {
                ArtworkImage(artwork: artwork)
                    .frame(width: 40, height: 40)
                    .cornerRadius(4.0)
            }
            VStack(alignment: .leading) {
                Text(item.title)
                Text(item.album)
                    .font(.footnote)
            }
            Spacer()
        }
        .padding()
        .onTapGesture {
            play(item: item)
        }
    }
    
    func play(item: PlayingItem) { Task {
        await playerBridge.play(item)
    }}
}

#Preview {
    EntryView(item: .mockData())
        .environment(\.playerBridge, MockedPlayerBridge())
}

extension PlayingItem {
    static func mockData() -> Self {
        .init(
            id: "1184710148",
            title: "Clocks",
            artist: "Vision Of Atlantis",
            duration: 0.0,
            album: "The Human Contradiction",
            url: nil,
            artwork: .remote(url: URL(string: "https://is1-ssl.mzstatic.com/image/thumb/Music122/v4/5d/3b/6b/5d3b6b36-3498-cfbc-bab1-ceb16d60f567/191018548728.jpg/1500x1500bb.jpg")!)
            )
    }
}
