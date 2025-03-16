//
//  Extensions.swift
//  iosApp
//
//  Created by Igor Ferreira on 14/03/2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import Shared
import SwiftUI
import MusicKit

#if os(macOS)
typealias UIImage = NSImage
typealias UIColor = NSColor
#endif

extension SongEntry: @retroactive Identifiable {}
extension SongEntry {
    var remoteArtwork: Bool { !artworkUrl.isEmpty && !artworkUrl.starts(with: "musicKit://") }
    var cacheArtwork: Bool { artworkUrl.starts(with: "data:image/jpeg;base64,") }
    var cachedImage: UIImage? {
        guard cacheArtwork else { return nil }
        let base64Data = artworkUrl.replacingOccurrences(of: "data:image/jpeg;base64,", with: "")
        guard let data = Data(base64Encoded: base64Data) else { return nil }
        return UIImage(data: data)
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
