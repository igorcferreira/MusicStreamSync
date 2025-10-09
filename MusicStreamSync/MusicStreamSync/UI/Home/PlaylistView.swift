//
//  PlaylistView.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 9/10/25.
//
import SwiftUI
import AppleMusicClient
import NukeUI

struct PlaylistView: View {
    @Environment(\.appleMusicClient) var appleMusicClient
    
    var body: some View {
        EntryListView(loadAction: {
            await appleMusicClient.fetchPlayslists()
        })
    }
}

#Preview {
    PlaylistView()
}
