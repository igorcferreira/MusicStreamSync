//
//  ScrobbleToolbar.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 30/10/25.
//
import Foundation
import SwiftUI
import LastFMClient

struct ScrobbleToolbar: ToolbarContent {
    @Environment(\.lastFMClient) private var lastFMClient
    @State private var autoScrobble: Bool = false
    
    var body: some ToolbarContent {
        ToolbarItem(placement: .primaryAction) {
            AuthenticationButton(client: lastFMClient)
        }
    }
}

#Preview {
    NavigationView {
        Text("Hello World!")
#if os(iOS)
            .toolbar { ScrobbleToolbar() }
#endif
    }
#if os(macOS)
    .toolbar { ScrobbleToolbar() }
#endif
}
