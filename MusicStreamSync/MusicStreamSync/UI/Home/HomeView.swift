//
//  HomeView.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 07/10/2025.
//
import SwiftUI
import LastFMClient
import AppleMusicClient
import NukeUI

struct HomeView: View {
    @Environment(\.appleMusicClient) var appleMusicClient
    
    var body: some View {
        EntryListView(loadAction: {
            await appleMusicClient.fetchLatestSongs()
        })
    }
}

#Preview {
    HomeView()
}
