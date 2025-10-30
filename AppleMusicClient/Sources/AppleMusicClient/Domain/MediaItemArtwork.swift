//
//  MediaItemArtwork.swift
//  AppleMusicClient
//
//  Created by Igor Ferreira on 30/10/25.
//
import Foundation
import MusicKit

extension Artwork {
    var url: URL? {
        let width = min(max(40, maximumWidth), 400)
        let height = min(max(40, maximumHeight), 400)
        return url(width: width, height: height)
    }
    
    var data: Data? {
        guard let url = url else {
            return nil
        }
        print("Artwork URL: \(url)")
        return try? Data(contentsOf: url)
    }
}

protocol MediaItemArtwork: MusicItem {
    var artwork: Artwork? { get }
}

extension MediaItemArtwork {
    var artworkURL: URL? {
        let fileManager = FileManager.default
        let temporaryDirectoryURL = fileManager.temporaryDirectory
        let filename = id.rawValue + ".jpg"
        let fileURL = temporaryDirectoryURL.appendingPathComponent(filename)
        
        if fileManager.fileExists(atPath: fileURL.path()) {
            return fileURL
        }
        
        guard let data = artwork?.data else {
            return nil
        }
        
        do {
            try data.write(to: fileURL)
            return fileURL
        } catch {
            return nil
        }
    }
}
