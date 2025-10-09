//
//  SearchView.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 9/10/25.
//
import SwiftUI

struct SearchView: View {
    @State private var search: String = ""
    @FocusState private var searchFocus: Bool
    
    var body: some View {
        Grouping {
            Text("SearchView")
                .searchable(text: $search)
        }
        .searchFocused($searchFocus)
        .onAppear {
            searchFocus = true
        }
    }
    
    @ViewBuilder
    func Grouping<Content: View>(
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
