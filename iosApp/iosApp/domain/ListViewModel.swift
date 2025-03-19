//
//  ListViewModel.swift
//  iosApp
//
//  Created by Igor Ferreira on 18/03/2025.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI
import MusicStream

protocol ListViewModel: ObservableObject {
    associatedtype ListEntry: EntryData
    
    var loading: Bool { get }
    var history: [ListEntry] { get }
    func load() async throws
}
