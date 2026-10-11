//
//  FlowObserverModifier.swift
//  iosApp
//
//  Created by Igor Ferreira on 01/12/2025.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI
import MusicStream

extension Observable where Self: AnyObject {
    @discardableResult
    func collect<Flow: SkieSwiftFlowProtocol, Setter>(
        _ flow: Flow,
        into path: ReferenceWritableKeyPath<Self, Setter>,
        mapper: @escaping (Flow.Element) -> Setter
    ) -> Task<Void, Never> where Flow.Failure == Never {
        Task.detached { [weak self] in
            for await output in flow {
                if Task.isCancelled { return }
                let mapped = mapper(output)
                Task.detached { @MainActor in
                    self?[keyPath: path] = mapped
                }
            }
        }
    }

    @discardableResult
    func collect<Flow: SkieSwiftFlowProtocol>(
        _ flow: Flow,
        into path: ReferenceWritableKeyPath<Self, Flow.Element>
    ) -> Task<Void, Never> where Flow.Failure == Never {
        collect(flow, into: path, mapper: { $0 })
    }

    @discardableResult
    func collect<Flow: SkieSwiftFlowProtocol, Output>(
        _ flow: Flow,
        into path: ReferenceWritableKeyPath<Self, [Output]>
    ) -> Task<Void, Never> where Flow.Element == NSArray?, Flow.Failure == Never {
        collect(flow, into: path, mapper: { $0 as? [Output] ?? [] })
    }

    @discardableResult
    func collect<Flow: SkieSwiftFlowProtocol>(
        _ flow: Flow,
        into path: ReferenceWritableKeyPath<Self, Bool>
    ) -> Task<Void, Never> where Flow.Element == KotlinBoolean, Flow.Failure == Never {
        collect(flow, into: path, mapper: { $0.boolValue })
    }
}
