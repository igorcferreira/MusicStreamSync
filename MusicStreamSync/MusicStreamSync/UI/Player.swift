//
//  Player.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 8/10/25.
//
import SwiftUI
import AppleMusicClient
import NukeUI

struct ArtworkImage: View {
    let artwork: PlayingItem.Artwork
    
    var body: some View {
        switch artwork {
        case let .local(data):
            localImage(data)
        case let .remote(url):
            remoteImage(url)
        }
    }
    
    @ViewBuilder
    func localImage(_ data: Data) -> some View {
        Image(uiImage: UIImage(data: data)!)
            .resizable()
    }
    
    @ViewBuilder
    func remoteImage(_ url: URL) -> some View {
        LazyImage(url: url) { phase in
            phase.image?.resizable()
        }
    }
}

struct PlayerView: View {
    @State private var playerBridge: PlayerBridge
    
    var isPlaying: Bool { playerBridge.isPlaying }
    var currentItem: PlayingItem? { playerBridge.currentItem }
    var label: String {
        isPlaying ? "pause.fill" : "play.fill"
    }
    
    init(playerBridge: PlayerBridge) {
        self.playerBridge = playerBridge
    }
    
    var body: some View {
        if let currentItem = playerBridge.currentItem {
            player(with: currentItem)
        } else {
            emptyPlayer()
        }
    }
    
    @ViewBuilder
    func emptyPlayer() -> some View {
        HStack(spacing: 8.0) {
            Image(systemName: "music.note.house.fill")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .padding(10.0)
                .foregroundStyle(Color.accent)
                .background(Color(uiColor: UIColor.secondarySystemBackground))
                .frame(width: 40.0, height: 40.0)
                .cornerRadius(4.0)
            VStack {
                Text(String(localized: "MusicStreamSync"))
                    .font(.caption)
                    .frame(maxWidth: .infinity, alignment: .leading)
                Text(String(localized: "Click to see your playlists and favourites"))
                    .font(.caption2)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
        }
        .padding(.horizontal, 28.0)
    }
    
    @ViewBuilder
    func player(with currentItem: PlayingItem) -> some View {
        HStack(spacing: 8.0) {
            if let artwork = currentItem.artwork {
                ArtworkImage(artwork: artwork)
                    .frame(width: 40.0, height: 40.0)
                    .cornerRadius(4.0)
            } else {
                Image(systemName: "music.note.house.fill")
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .padding(10.0)
                    .foregroundStyle(Color.accent)
                    .background(Color(uiColor: UIColor.secondarySystemBackground))
                    .frame(width: 40.0, height: 40.0)
                    .cornerRadius(4.0)
            }
            VStack {
                Text(currentItem.title)
                    .font(.caption)
                    .frame(maxWidth: .infinity, alignment: .leading)
                Text(currentItem.artist)
                    .font(.caption2)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            
            Button {
                toggle()
            } label: {
                Image(systemName: label)
                    .foregroundStyle(Color.accent)
            }
        }
        .padding(.horizontal, 28.0)
    }
    
    func toggle() { Task {
        if playerBridge.isPlaying {
            await playerBridge.pause()
        } else {
            await playerBridge.play()
        }
    }}
}

#Preview("No item available") {
    PlayerView(playerBridge: MockedPlayerBridge(isPlaying: false))
        .frame(height: 54.0)
        .frame(maxWidth: .infinity)
        .glassEffect()
        .padding()
}

#Preview("Playing") {
    PlayerView(playerBridge: MockedPlayerBridge(
        isPlaying: true,
        currentItem: .mockData()
    ))
    .frame(height: 54.0)
    .frame(maxWidth: .infinity)
    .glassEffect()
    .padding()
}

#Preview("No artwork") {
    PlayerView(playerBridge: MockedPlayerBridge(
        isPlaying: true,
        currentItem: .mockData()
    ))
    .frame(height: 54.0)
    .frame(maxWidth: .infinity)
    .glassEffect()
    .padding()
}
