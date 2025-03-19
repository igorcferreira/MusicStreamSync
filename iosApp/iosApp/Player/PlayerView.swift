//
//  PlayerView.swift
//  iosApp
//
//  Created by Igor Ferreira on 15/03/2025.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI
import MusicStream
import NukeUI
import MusicKit

struct PlayerView: View {
    
    @StateObject private var viewModel: PlayerViewModel
    
    init(viewModel: PlayerViewModel) {
        self._viewModel = .init(wrappedValue: viewModel)
    }
    
    init(factory: Factory) {
        self._viewModel = .init(wrappedValue: .init(factory: factory))
    }
    
    var item: MusicEntry? {
        viewModel.playingItem
    }
    
    var actionImage: String {
        #if os(macOS)
        "music.note.house.fill"
        #else
        if viewModel.isPlaying {
            "pause.fill"
        } else {
            "play.fill"
        }
        #endif
    }
    
    @ViewBuilder
    func player(for item: MusicEntry) -> some View {
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
            } else {
                Image(systemName: "music.note")
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .padding(10.0)
                    .frame(width: 40, height: 40)
                    .background(Color(uiColor: UIColor.secondarySystemBackground), in: RoundedRectangle(cornerRadius: 4.0))
                    .foregroundStyle(Color(uiColor: UIColor.tertiarySystemGroupedBackground))
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
                    .transition(.move(edge: .top))
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

#if DEBUG
#Preview {
    @Previewable @State var entry: MusicEntry = .init(
        id: "1184710148",
        title: "Clocks",
        artist: "Vision Of Atlantis",
        artworkUrl: "https://is1-ssl.mzstatic.com/image/thumb/Music122/v4/5d/3b/6b/5d3b6b36-3498-cfbc-bab1-ceb16d60f567/191018548728.jpg/1500x1500bb.jpg",
        album: "The Human Contradiction"
    )
    @Previewable @Environment(\.factory) var factory
    
    PlayerView(viewModel: MockedPlayerViewModel(
        isPlaying: false,
        playingItem: entry
    )).colorScheme(.light)
    
    PlayerView(viewModel: MockedPlayerViewModel(
        isPlaying: true,
        playingItem: entry
    )).colorScheme(.dark)
}
#endif
