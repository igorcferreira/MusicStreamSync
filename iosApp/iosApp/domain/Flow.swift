//
//  Flow.swift
//  iosApp
//
//  Created by Igor Ferreira on 12/03/2025.
//  Copyright © 2025 orgName. All rights reserved.
//
import MusicStream

class BlockedCollector<Value>: NSObject, Kotlinx_coroutines_coreFlowCollector {
    let sink: (Value) async -> Void
    
    init(sink: @escaping (Value) async -> Void) {
        self.sink = sink
    }
    
    func emit(value: Any?) async throws {
        guard let converted = value as? Value else { return }
        await self.sink(converted)
    }
}

extension Kotlinx_coroutines_coreStateFlow {
    func collect<T: AnyObject, V>(into path: WritableKeyPath<T, V>, observer: T) {
        weak var holderReference = observer
        if let initial = self.value as? V {
            holderReference?[keyPath: path] = initial
        }
        sinkOnMain({ holderReference?[keyPath: path] = $0 })
    }
    
    func sinkOnMain<Value>(_ block: @MainActor @escaping (Value) async -> Void) { Task {
        try await self.collect(collector: BlockedCollector<Value>(sink: { value in
            Task { @MainActor in await block(value) }
        }))
    }}
    
    func sink<Value>(_ block: @escaping (Value) async -> Void) { Task {
        try await self.collect(collector: BlockedCollector(sink: block))
    }}
}

extension Kotlinx_coroutines_coreSharedFlow {
    func collect<T: AnyObject, V>(into path: WritableKeyPath<T, V>, observer: T) {
        weak var holderReference = observer
        sinkOnMain({ holderReference?[keyPath: path] = $0 })
    }
    
    func sinkOnMain<Value>(_ block: @MainActor @escaping (Value) async -> Void) { Task {
        try await self.collect(collector: BlockedCollector<Value>(sink: { value in
            Task { @MainActor in await block(value) }
        }))
    }}
    
    func sink<Value>(_ block: @escaping (Value) async -> Void) { Task {
        try await self.collect(collector: BlockedCollector(sink: block))
    }}
}
