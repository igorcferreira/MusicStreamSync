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
    
    init(loadAction: @Sendable @escaping () async -> [PlayingItem]) {
        self.loadAction = loadAction
    }
    
    var body: some View {
        List {
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
        await update(items: await loadAction())
    }}
    
    private func update(items: [PlayingItem]) {
        self.items = items
    }
}

#Preview {
    EntryListView(loadAction: {
        [.mockData()]
    })
}
