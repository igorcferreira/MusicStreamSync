//
//  MusicStreamSyncApp.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 7/10/25.
//

import SwiftUI

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
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
