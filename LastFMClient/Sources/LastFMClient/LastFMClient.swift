// The Swift Programming Language
// https://docs.swift.org/swift-book
import Foundation

public struct LastFMClient: Sendable {
    
    private var keyConfiguration: KeyConfiguration? {
        get {
            KeyConfiguration.restore()
        }
        
        set {
            newValue.store()
        }
    }
    
    public init(
        apiKey: String,
        apiSecret: String
    ) {
        self.keyConfiguration = .init(apiKey: apiKey, apiSecret: apiSecret)
    }
    
}
