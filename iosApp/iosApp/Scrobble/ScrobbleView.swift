//
//  ScrobbleView.swift
//  iosApp
//
//  Created by Igor Ferreira on 24/3/25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import MusicStream

struct ScrobbleView: View {
    
    @Environment(\.factory) var factory
    @ObservedObject var recentlyPlayed: RecentlyPlayedViewModel
    @ObservedObject var lastFMViewModel: LastFMViewModel
    @State var selection = Set<MusicEntry>()    
    
    init(factory: Factory) {
        self.recentlyPlayed = RecentlyPlayedViewModel(useCase: factory.makeRecentlyPlayedUseCase())
        self.lastFMViewModel = LastFMViewModel()
    }
    
    init(recentlyPlayed: RecentlyPlayedViewModel, lastFMViewModel: LastFMViewModel) {
        self.recentlyPlayed = recentlyPlayed
        self.lastFMViewModel = lastFMViewModel
    }
    
    @ViewBuilder
    private var scrobbleButton: some View {
        Button("Scrobble") {
            scrobble()
        }
        .disabled(selection.isEmpty)
    }
    
    var body: some View {
        VStack {
#if os(iOS)
            HStack {
                Text("Manual Scrobble")
                    .font(.title)
                    .frame(maxWidth: .infinity, alignment: .leading)
                Spacer()
                LastFMToolbarItem()
                scrobbleButton
            }
#endif
            if recentlyPlayed.loading {
                ProgressView()
                    .accessibilityIdentifier("loading")
                    .accessibilityLabel(Text("Loading..."))
                    .listRowSeparator(.hidden)
                    .id("loding_indicator")
            }
            List(selection: $selection) {
                if !recentlyPlayed.loading {
                    Text("Select the items to scrobble and then hit 'Scrobble' to update your Last.fm library")
                        .font(.body)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .listRowSeparator(.hidden)
                }
                ForEach(recentlyPlayed.history) { entry in
                    EntryView(entry: entry, factory: factory) { item in
                        toggle(selection: item)
                    }
                    .overlay {
                        if (isSelected(entry)) {
                            RoundedRectangle(cornerRadius: 4.0)
                            #if os(iOS)
                                .foregroundStyle(Color(uiColor: UIColor.tintColor.withAlphaComponent(0.33)))
                            #else
                                .foregroundStyle(Color(nsColor: NSColor.selectedContentBackgroundColor.withAlphaComponent(0.33)))
                            #endif
                                .onTapGesture { toggle(selection: entry) }
                        }
                    }
                }
            }
            .refreshable {
                await self.load()
            }
        }
        .navigationTitle("Manual Scrobble")
        .padding()
        .onFirstTask {
            await self.load()
        }
        .toolbar { scrobbleButton }
        .contextMenu {
            Button("Refresh") { Task {
                await self.load()
            }}
            .keyboardShortcut("r", modifiers: [.command])
        }
    }
    
    private func load() async {
        try? await recentlyPlayed.load()
    }
    
    private func isSelected(_ entry: MusicEntry) -> Bool {
        return selection.contains(entry)
    }
    
    private func toggle(selection: MusicEntry) {
        if isSelected(selection) {
            self.selection.remove(selection)
        } else {
            self.selection.insert(selection)
        }
    }
    
    private func scrobble() { Task {
        await lastFMViewModel.scrobble(selection)
        selection.removeAll()
    }}
}

#Preview {
    @Previewable @Environment(\.factory) var factory
    ScrobbleView(factory: factory)
}
