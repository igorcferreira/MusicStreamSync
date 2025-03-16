//
//  PlayerView.swift
//  iosApp
//
//  Created by Igor Ferreira on 15/03/2025.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI
import Shared
import NukeUI
import MusicKit

struct PlayerView: View {
    
    @ObservedObject private var viewModel: PlayerViewModel
#if DEBUG
    @State var mockItem: SongEntry?
    init(factory: Factory, mockItem: SongEntry? = nil) {
        self.viewModel = .init(factory: factory)
        self._mockItem = .init(initialValue: mockItem)
    }
#else
    init(factory: Factory) {
        self.viewModel = .init(factory: factory)
    }
#endif
    
    var item: SongEntry? {
#if DEBUG
        viewModel.playingItem ?? mockItem
#else
        viewModel.playingItem
#endif
    }
    
    var actionImage: String {
        if viewModel.isPlaying {
            "pause.fill"
        } else {
            "play.fill"
        }
    }
    
    @ViewBuilder
    func player(for item: SongEntry) -> some View {
        HStack {
            if let image = item.cachedImage {
                Image(uiImage: image)
                    .resizable()
                    .frame(width: 40, height: 40)
                    .clipShape(RoundedRectangle(cornerRadius: 4.0))
            } else if item.remoteArtwork {
                LazyImage(url: URL(string: item.artworkUrl)) { phase in
                    phase.image?.resizable()
                }
                .frame(width: 40, height: 40)
                .clipShape(RoundedRectangle(cornerRadius: 4.0))
            }
            
            VStack(alignment: .leading) {
                Text(item.title)
                    .font(.body)
                    .lineLimit(1)
                Text(item.artist)
                    .font(.footnote)
                    .lineLimit(1)
            }
            
            Spacer()
            
            Image(systemName: actionImage)
                .transition(.symbolEffect(.automatic))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(Color(uiColor: UIColor.tertiarySystemGroupedBackground))
        .onTapGesture { action() }
    }
    
    var body: some View {
        ZStack {
            if let item {
                player(for: item)
                    .transition(.move(edge: .bottom))
            }
        }
        .animation(.default, value: item)
    }
    
    private func action() {
        if viewModel.isPlaying {
            viewModel.pause()
        } else {
            viewModel.play()
        }
    }
}

#Preview {
    @Previewable @State var entry: SongEntry = .init(
        id: "1184710148",
        title: "Clocks",
        artist: "Vision Of Atlantis",
        artworkUrl: "https://is1-ssl.mzstatic.com/image/thumb/Music122/v4/5d/3b/6b/5d3b6b36-3498-cfbc-bab1-ceb16d60f567/191018548728.jpg/1500x1500bb.jpg",
        album: "The Human Contradiction"
    )
    @Previewable @Environment(\.factory) var factory
    
    PlayerView(factory: factory, mockItem: entry)
        .colorScheme(.light)
    
    PlayerView(factory: factory, mockItem: entry)
        .colorScheme(.dark)
}
