//
//  Extensions.swift
//  iosApp
//
//  Created by Igor Ferreira on 14/03/2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import MusicStream
import SwiftUI
import MusicKit

#if os(macOS)
typealias UIImage = NSImage
typealias UIColor = NSColor
#endif

extension MusicEntry: @retroactive Identifiable {}
extension MusicEntry {
    var remoteArtwork: Bool { !artworkUrl.isEmpty && !artworkUrl.starts(with: "musicKit://") }
    var cacheArtwork: Bool { artworkUrl.starts(with: "data:image/jpeg;base64,") }
    var cachedImage: UIImage? {
        guard cacheArtwork else { return nil }
        let base64Data = artworkUrl.replacingOccurrences(of: "data:image/jpeg;base64,", with: "")
        guard let data = Data(base64Encoded: base64Data) else { return nil }
        return UIImage(data: data)
    }
}

extension PlayerUseCase {
    func play(entry: any EntryData) {
        if let music = entry as? MusicEntry {
            play(item: music)
        } else if let playlist = entry as? PlaylistEntry {
            play(playlist: playlist)
        }
    }
}

#if os(macOS)
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
