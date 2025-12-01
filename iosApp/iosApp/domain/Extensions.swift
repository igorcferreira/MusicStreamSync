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
