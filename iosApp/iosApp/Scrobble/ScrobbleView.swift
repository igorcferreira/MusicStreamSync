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
    @State var selection = [MusicEntry]()
    
    init(factory: Factory) {
        self._recentlyPlayed = .init(initialValue: RecentlyPlayedViewModel(useCase: factory.makeRecentlyPlayedUseCase()))
        self._lastFMViewModel = .init(initialValue: LastFMViewModel())
    }
    
    init(recentlyPlayed: RecentlyPlayedViewModel, lastFMViewModel: LastFMViewModel) {
        self._recentlyPlayed = .init(initialValue: recentlyPlayed)
        self._lastFMViewModel = .init(initialValue: lastFMViewModel)
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
            } else {
                Text("Select the items to scrobble and then hit 'Scrobble' to update your Last.fm library")
                    .font(.body)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .id("header_text")
            }
            
            List(recentlyPlayed.history) { entry in
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
                .id(entry.id)
            }
            .listStyle(.plain)
            .refreshable { self.load() }
        }
        .navigationTitle("Manual Scrobble")
        .padding()
        .toolbar { scrobbleButton }
        .contextMenu {
            Button("Refresh") {
                self.load()
            }
            .keyboardShortcut("r", modifiers: [.command])
        }
    }
    
    private func load() { Task {
        try? await recentlyPlayed.load()
    }}
    
    private func isSelected(_ entry: MusicEntry) -> Bool {
        return selection.contains(entry)
    }
    
    private func toggle(selection: MusicEntry) {
        if isSelected(selection) {
            self.selection.removeAll(where: {
                $0.id == selection.id
            })
        } else {
            self.selection.append(selection)
        }
    }
    
    private func scrobble() { Task {
        await lastFMViewModel.scrobble(
            selection,
            items: recentlyPlayed.history
        )
        selection.removeAll()
    }}
}

#Preview {
    @Previewable @Environment(\.factory) var factory
    ScrobbleView(factory: factory)
}
