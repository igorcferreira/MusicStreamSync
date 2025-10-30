//
//  ScrobbleView.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 30/10/25.
//
import SwiftUI
import Foundation

struct ScrobbleView: View {
    @AppStorage("auto_scrobble") var autoScrobble: Bool = true

    var body: some View {
        VStack {
            Toggle("Auto Scrobble", isOn: $autoScrobble)
            Spacer()
        }
        .padding()
    }
}

#Preview {
    ScrobbleView()
}
