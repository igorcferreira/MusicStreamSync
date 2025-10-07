//
//  LastFMLoginView.swift
//  LastFMClient
//
//  Created by Igor Ferreira on 7/10/25.
//
import SwiftUI
import WebKit

public struct LastFMLoginView: View {
    private let apiKey: String
    private let tokenCompleted: @Sendable (String) async -> Void
    private var loginURL: String {
        "https://www.last.fm/api/auth/?api_key=\(apiKey)&cb=musicstreamsync://login"
    }
    @State private var page = {
        var configuration = WebPage.Configuration()
        configuration.upgradeKnownHostsToHTTPS = true
        configuration.applicationNameForUserAgent = "MusicStreamSync"
        configuration.websiteDataStore = WKWebsiteDataStore.nonPersistent()
        return WebPage(configuration: configuration)
    }()
    
    init(apiKey: String, tokenCompleted: @escaping @Sendable (String) async -> Void) {
        self.apiKey = apiKey
        self.tokenCompleted = tokenCompleted
    }
    
    public var body: some View {
        WebView(page)
            .task {
                page.load(URL(string: loginURL))
            }
            .webViewBackForwardNavigationGestures(.disabled)
            .webViewLinkPreviews(.disabled)
            .onChange(of: page.url) { _, newValue in
                Task.detached { await analyse(newValue) }
            }
    }
    
    @concurrent
    private func analyse(_ url: URL?) async {
        guard let url else { return }
        guard let components = URLComponents(url: url, resolvingAgainstBaseURL: false) else {
            return
        }
        guard components.scheme == "musicstreamsync" else { return }
        guard components.host == "login" else { return }
        guard let token = components.queryItems?.first(where: { $0.name == "token" })?.value else {
            return
        }
        
        await tokenCompleted(token)
    }
}

#Preview {
    LastFMLoginView(apiKey: "") { token in
        print("Token: \(token)")
    }
}

public extension LastFMClient {
    @ViewBuilder
    func loginView(
        completion: @Sendable @escaping (Bool) async -> Void
    ) -> some View {
        let apiKey = keyConfiguration?.apiKey ?? ""
        LastFMLoginView(apiKey: apiKey) { token in
            do {
                let _ = try await validate(token: token)
                await completion(true)
            } catch {
                await completion(false)
            }
        }
    }
}
