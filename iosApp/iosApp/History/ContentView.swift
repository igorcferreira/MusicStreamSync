import SwiftUI
import Shared
import MusicKit

struct ContentView: View {
    @Environment(\.factory) private var factory
    @ObservedObject private var viewModel: RecentlyPlayedViewModel
    
    init(factory: Factory) {
        viewModel = .init(useCase: factory.makeRecentlyPlayedUseCase())
    }
    
    var body: some View {
        VStack {
            if viewModel.loading {
                ProgressView()
                    .accessibilityIdentifier("loading")
            } else {
                Text("Recently Played")
                    .accessibilityIdentifier("home_title")
                    .font(.title)
                
                List {
                    ForEach(viewModel.history)  { entry in
                        EntryView(entry: entry, factory: factory)
                    }
                    if !viewModel.history.isEmpty {
                        ZStack {}
                            .frame(height: 50)
                            .id("bottom_padding")
                    }
                }
                .listStyle(.plain)
            }
        }
        .ignoresSafeArea(.container, edges: .bottom)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .padding([.leading, .top, .bottom])
        .task { await self.load() }
        .overlay(alignment: .bottom) {
            PlayerView(factory: factory)
        }
    }
    
    private func load() async {
        do {
            try await viewModel.updateHistory()
        } catch {
            print("Error: \(error)")
        }
    }
}

#Preview("Light Mode") {
    @Previewable @Environment(\.factory) var factory
    ContentView(factory: factory)
        .colorScheme(.light)
}

#Preview("Dark Mode") {
    @Previewable @Environment(\.factory) var factory
    ContentView(factory: factory)
        .colorScheme(.dark)
}
