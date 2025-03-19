//
//  EntryView.swift
//  iosApp
//
//  Created by Igor Ferreira on 13/03/2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import MusicStream
import NukeUI

struct EntryView<E: EntryData>: View {
    @State var entry: E
    private let playerUseCase: PlayerUseCase
    
    init(entry: E, factory: Factory) {
        self.entry = entry
        self.playerUseCase = factory.makePlayerUseCase()
    }
    
    var body: some View {
        HStack(alignment: .center, spacing: 8.0) {
            if let artwork = entry.artworkUrl {
                LazyImage(url: URL(string: artwork)) { phase in
                    phase.image?.resizable()
                }
                .frame(width: 60, height: 60)
                .clipShape(RoundedRectangle(cornerRadius: 4.0))
            } else {
                Image(systemName: "music.quarternote.3")
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .padding(10.0)
                    .frame(width: 60, height: 60)
                    .background(Color(uiColor: UIColor.tertiarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 4.0))
                    .foregroundStyle(Color(uiColor: UIColor.secondarySystemBackground))
            }
            
            VStack(alignment: .leading) {
                Text(entry.title)
                    .font(.headline)
                if let album = entry.body, !album.isEmpty {
                    Text(album)
                        .font(.body)
                }
                if let footer = entry.footer, !footer.isEmpty {
                    Text(footer)
                        .font(.caption)
                }
            }
            Spacer()
        }
        .padding()
        .background(Color(uiColor: UIColor.secondarySystemBackground), in: RoundedRectangle(cornerRadius: 4.0))
        .listRowSeparator(.hidden)
        .onTapGesture { playerUseCase.play(entry: entry) }
        .accessibilityAddTraits(.isButton)
        .accessibilityElement(children: .combine)
        .accessibilityIdentifier("entry_\(entry.id)")
        .accessibilityLabel("Play \(entry.title) - \(entry.body ?? "")")
        .id(entry.id)
    }
}

#Preview {
    @Previewable @State var entry: MusicEntry = .init(
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
