//
//  SearchView.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 9/10/25.
//
import SwiftUI
import AppleMusicClient
import NukeUI

struct SearchView: View {
    @Environment(\.appleMusicClient) var appleMusicClient
    @Environment(\.playerBridge) var playerBridge
    @State private var search: String = ""
    @FocusState private var searchFocus: Bool
    @State private var items: [PlayingItem] = []
    
    var body: some View {
        Grouping {
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
            .searchable(text: $search)
        }
        .searchFocused($searchFocus)
        .task {
            searchFocus = true
        }
        .onChange(of: search) { _, newValue in
            search(term: newValue)
        }
    }
    
    func play(item: PlayingItem) { Task {
        await playerBridge.play(item)
    }}
    
    private func search(term: String) { Task {
        let trimmed = term.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else {
            await update(search: [])
            return
        }
        
        let items = await appleMusicClient.searchSong(term: trimmed)
        await update(search: items)
    }}
    
    @MainActor
    private func update(search: [PlayingItem]) async {
        items = search
    }
    
    @ViewBuilder
    private func Grouping<Content: View>(
        @ViewBuilder body: () -> Content
    ) -> some View {
        #if os(macOS)
        body()
        #else
        NavigationView {
            body()
        }
        #endif
    }
}
