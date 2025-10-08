//
//  PlayerBridge.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 8/10/25.
//
import Foundation
import SwiftUI

protocol PlayerBridge: Sendable, Observable {
    var isPlaying: Bool { get }
    var currentItem: PlayingItem? { get }
    func play() async
    func pause() async
    func getCurrentItem() async -> PlayingItem?
}

func fetchSystemBridge() -> PlayerBridge {
    #if os(macOS)
    MacOSPlayerBridge()
    #else
    MediaKitPlayerBridge()
    #endif
}
