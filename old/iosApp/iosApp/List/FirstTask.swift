//
//  FirstTask.swift
//  iosApp
//
//  Created by Igor Ferreira on 18/03/2025.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI

extension View {
  func onFirstTask(_ action: @escaping @Sendable () async -> ()) -> some View {
    modifier(FirstTask(action: action))
  }
}

private struct FirstTask: ViewModifier {
  let action: @Sendable () async -> ()

  // Use this to ensure the block is only executed once
    @State private var hasAppeared = false

  func body(content: Content) -> some View {
    content.task {
      // Prevent the action from being executed more than once
      guard !hasAppeared else { return }
      hasAppeared = true
      await action()
    }
  }
}

// Create a separate class to hold the isFirstAppear state
private class FirstAppearState: ObservableObject {
    @Published var value: Bool = false
}
