//
//  EntryView.swift
//  iosApp
//
//  Created by Igor Ferreira on 13/03/2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import Shared
import NukeUI

struct EntryView: View {
    @State var entry: SongEntry
    private let playerUseCase: PlayerUseCase
    
    init(entry: SongEntry, factory: Factory) {
        self.entry = entry
        self.playerUseCase = factory.makePlayerUseCase()
    }
    
    var body: some View {
        HStack(alignment: .center, spacing: 8.0) {
            LazyImage(url: URL(string: entry.artworkUrl)) { phase in
                phase.image?.resizable()
            }
            .frame(width: 60, height: 60)
            .clipShape(RoundedRectangle(cornerRadius: 4.0))
            
            VStack(alignment: .leading) {
                Text(entry.title)
                    .font(.headline)
                if let album = entry.album {
                    Text(album)
                        .font(.body)
                }
                Text(entry.artist)
                    .font(.caption)
            }
            Spacer()
        }
        .padding()
        .background(Color(uiColor: UIColor.secondarySystemBackground), in: RoundedRectangle(cornerRadius: 4.0))
        .listRowSeparator(.hidden)
        .onTapGesture { playerUseCase.play(item: entry) }
        .accessibilityAddTraits(.isSummaryElement)
        .accessibilityElement(children: .combine)
        .accessibilityIdentifier("entry_\(entry.id)")
        .accessibilityLabel("\(entry.title) by \(entry.artist)")
        .id(entry.id)
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
    
    EntryView(entry: entry, factory: factory)
        .colorScheme(.light)
    EntryView(entry: entry, factory: factory)
        .colorScheme(.dark)
}
