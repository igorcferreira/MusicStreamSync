//
//  MusicStreamSyncApp.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 7/10/25.
//

import SwiftUI
import AppleMusicClient
import ArkanaKeys

struct AppleMusicClientKey: EnvironmentKey {
    static let defaultValue: AppleMusicClient = .init(
        teamId: ArkanaKeys.Global().teamId,
        keyId: ArkanaKeys.Global().keyId,
        privateKey: ArkanaKeys.Global().privateKey
    )
}

public extension EnvironmentValues {
    var appleMusicClient: AppleMusicClient {
        get {
            self[AppleMusicClientKey.self]
        } set {
            self[AppleMusicClientKey.self] = newValue
        }
    }
}

#if os(macOS)
typealias UIColor = NSColor
typealias UIImage = NSImage
extension NSColor {
    static var secondarySystemBackground: UIColor {
        NSColor.underPageBackgroundColor
    }
    static var tertiarySystemGroupedBackground: UIColor {
        NSColor.controlBackgroundColor
    }
}
extension Color {
    init(uiColor color: UIColor) {
        self.init(nsColor: color)
    }
}
extension Image {
    init(uiImage image: UIImage) {
        self.init(nsImage: image)
    }
}
#endif

@main
struct MusicStreamSyncApp: App {
    @Environment(\.appleMusicClient) var appleMusicClient
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .task { await appleMusicClient.authorize() }
        }
    }
}
