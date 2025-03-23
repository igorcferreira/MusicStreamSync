//
//  SyncMenuBar.swift
//  iosApp
//
//  Created by Igor Ferreira on 23/03/2025.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI

#if os(macOS)
struct SyncMenuBar: View {
    
    @Environment(\.openWindow) var openWindow
    @Environment(\.factory) var factory
    
    var body: some View {
        PlayerView(factory: factory) { openApp() }
    }
    
    private func openApp() {
        let mainWindows = NSApplication.shared.windows.contains(where: {
            $0.identifier?.rawValue.starts(with: "main") ?? false
        })
        if !mainWindows {
            openWindow(id: "main")
        }
        NSApplication.shared.windows.forEach { window in
            if window.identifier?.rawValue.starts(with: "main") ?? false {
                window.makeKeyAndOrderFront(window)
                window.orderFrontRegardless()
                return
            }
        }
    }
}
#endif
