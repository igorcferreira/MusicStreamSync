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

extension AsyncSequence where Self: Sendable, Element: Sendable {
    func finishOnError() -> AsyncStream<Element> {
        AsyncStream { continuation in
            let task = Task {
                do {
                    for try await element in self { continuation.yield(element) }
                } catch {}
                continuation.finish()
            }
            continuation.onTermination = { _ in task.cancel() }
        }
    }
}

extension Observable where Self: AnyObject {
    @discardableResult
    func collect<Output, Setter, Failure: Error>(
        _ flow: @escaping NativeFlow<Output, Failure, KotlinUnit>,
        into path: ReferenceWritableKeyPath<Self, Setter>,
        mapper: @escaping (Output) -> Setter
    ) -> Task<Void, Error> {
        Task.detached { [weak self] in
            let sequence = asyncSequence(for: flow)
                .finishOnError()
            for await output in sequence {
                if Task.isCancelled { return }
                let mapped = mapper(output)
                Task.detached { @MainActor in
                    self?[keyPath: path] = mapped
                }
            }
        }
    }

    @discardableResult
    func collect<Output, Failure: Error>(
        _ flow: @escaping NativeFlow<Output, Failure, KotlinUnit>,
        into path: ReferenceWritableKeyPath<Self, Output>
    ) -> Task<Void, Error>  {
        collect(flow, into: path, mapper: { $0 })
    }

    @discardableResult
    func collect<Output, Failure: Error>(
        _ flow: @escaping NativeFlow<NSArray?, Failure, KotlinUnit>,
        into path: ReferenceWritableKeyPath<Self, [Output]>
    ) -> Task<Void, Error> {
        collect(flow, into: path, mapper: { $0 as? [Output] ?? [] })
    }

    @discardableResult
    func collect<Failure: Error>(
        _ flow: @escaping NativeFlow<KotlinBoolean, Failure, KotlinUnit>,
        into path: ReferenceWritableKeyPath<Self, Bool>
    ) -> Task<Void, Error> {
        collect(flow, into: path, mapper: { $0.boolValue })
    }
}
