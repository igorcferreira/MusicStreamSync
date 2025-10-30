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
    @State private var loading: Bool = false
    @State private var currentSearch: Task<Void, Never>? = nil
    
    var body: some View {
        Grouping {
            List {
                if loading {
                    VStack(alignment: .center) {
                        ProgressView()
                    }
                    .frame(maxWidth: .infinity)
                }
                ForEach(items) { item in
                    EntryView(item: item)
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
    
    private func search(term: String) {
        currentSearch?.cancel()
        currentSearch = Task.detached {
            let trimmed = term.trimmingCharacters(in: .whitespacesAndNewlines)
            guard !trimmed.isEmpty else {
                await update(search: [])
                return
            }
            await set(loading: true)
            let items = await appleMusicClient.searchSong(term: trimmed)
            await update(search: items)
            await set(loading: false)
        }
    }
    
    @MainActor
    private func set(loading: Bool) {
        self.loading = loading
    }
    
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
