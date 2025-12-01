import SwiftUI
import MusicStream
import MusicKit

struct ContentView<VM: ListViewModel>: View {
    @Environment(\.factory) private var factory
    @State private var title: String
    @StateObject private var viewModel: VM
    
    init(title: String, viewModel: VM) {
        self._title = .init(initialValue: title)
        self._viewModel = .init(wrappedValue: viewModel)
    }
    
    var body: some View {
        List {
            if viewModel.loading {
                ProgressView()
                    .accessibilityIdentifier("loading")
                    .accessibilityLabel(Text("Loading..."))
                    .frame(maxWidth: .infinity, alignment: .center)
                    .listRowSeparator(.hidden)
                    .id(UUID())
            }
            
            ForEach(viewModel.history, id: \.id) { entry in
                EntryView(entry: entry, factory: factory)
            }
        }
        .listStyle(.plain)
        .refreshable { Task { await self.load() }}
        .ignoresSafeArea(.container, edges: .bottom)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .padding()
        .navigationTitle(title)
        .onFirstTask { await self.load() }
        .contextMenu {
            Button("Refresh") { Task {
                await self.load()
            }}
            .keyboardShortcut("r", modifiers: [.command])
        }
        .navigationBarTitleDisplayMode(.inline)
    }
    
    private func load() async {
        do {
            try await viewModel.load()
        } catch {
            print("Error: \(error)")
        }
    }
}

#Preview("Light Mode") {
    @Previewable @Environment(\.factory) var factory
    ContentView(
        title: "Recently Played",
        viewModel: RecentlyPlayedViewModel(useCase: factory.makeRecentlyPlayedUseCase())
    ).colorScheme(.light)
}

#Preview("Dark Mode") {
    @Previewable @Environment(\.factory) var factory
    ContentView(
        title: "Playlists",
        viewModel: PlaylistViewModel(useCase: factory.makePlaylistsUseCase())
    ).colorScheme(.dark)
}
