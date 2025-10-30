//
//  EntryListView.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 9/10/25.
//
import SwiftUI
import AppleMusicClient

struct EntryListView: View {
    let loadAction: @Sendable () async -> [PlayingItem]
    @State private var items: [PlayingItem] = []
    @State private var loading: Bool = false
    
    init(loadAction: @Sendable @escaping () async -> [PlayingItem]) {
        self.loadAction = loadAction
    }
    
    var body: some View {
        List {
            if loading {
                VStack(alignment: .center) {
                    ProgressView()
                }
                .frame(maxWidth: .infinity)
            }
            ForEach(Array(items.enumerated()), id: \.offset) { _, item in
                EntryView(item: item)
            }
        }
        .onAppear {
            loadList()
        }
        .refreshable {
            loadList()
        }
        .onReceive(NotificationCenter.default.publisher(
            for: AppleMusicClient.authenticationChange
        )) { _ in
            loadList()
        }
    }
    
    private func loadList() { Task.detached {
        await set(loading: true)
        await update(items: await loadAction())
        await set(loading: false)
    }}
    
    @MainActor
    private func set(loading: Bool) {
        self.loading = loading
    }
    
    @MainActor
    private func update(items: [PlayingItem]) {
        self.items = items
    }
}

#Preview {
    EntryListView(loadAction: {
        [.mockData()]
    })
}
