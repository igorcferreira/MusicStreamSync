import SwiftUI
import Shared
import ArkanaKeys
import MusicKit

@main
struct iOSApp: App {
    @Environment(\.factory) var factory
    
    var body: some Scene {
        WindowGroup {
            ContentView(factory: factory)
        }
    }
}
