//
//  FlowObserverModifier.swift
//  iosApp
//
//  Created by Igor Ferreira on 01/12/2025.
//  Copyright © 2025 orgName. All rights reserved.
//
import Combine
import KMPNativeCoroutinesCore
import KMPNativeCoroutinesAsync
import SwiftUI
import MusicStream

extension Observable where Self: AnyObject {
    func collect<Output, Setter, Failure: Error>(
        _ flow: @escaping NativeFlow<Output, Failure, KotlinUnit>,
        into path: ReferenceWritableKeyPath<Self, Setter>,
        mapper: @escaping (Output) -> Setter
    ) { Task.detached { [weak self] in
        let sequence = asyncSequence(for: flow)
        for try await output in sequence {
            if self == nil { return }
            let mapped = mapper(output)
            Task.detached { @MainActor in self?[keyPath: path] = mapped }
        }
    }}
    
    func collect<Output, Failure: Error>(
        _ flow: @escaping NativeFlow<Output, Failure, KotlinUnit>,
        into path: ReferenceWritableKeyPath<Self, Output>
    ) {
        collect(flow, into: path, mapper: { $0 })
    }

    func collect<Output, Failure: Error>(
        _ flow: @escaping NativeFlow<NSArray?, Failure, KotlinUnit>,
        into path: ReferenceWritableKeyPath<Self, [Output]>
    ) {
        collect(flow, into: path, mapper: { $0 as? [Output] ?? [] })
    }

    func collect<Failure: Error>(
        _ flow: @escaping NativeFlow<KotlinBoolean, Failure, KotlinUnit>,
        into path: ReferenceWritableKeyPath<Self, Bool>
    ) {
        collect(flow, into: path, mapper: { $0.boolValue })
    }
}
