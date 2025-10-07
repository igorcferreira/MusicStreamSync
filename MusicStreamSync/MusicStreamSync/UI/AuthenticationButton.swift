//
//  AuthenticationButton.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 07/10/2025.
//
import SwiftUI
import LastFMClient

struct AuthenticationButton: View {
    @State private var showAuthentication: Bool = false
    @State private var isAuthenticated: Bool = false
    @Environment(\.lastFMClient) private var client
    
    var body: some View {
        Group {
            if isAuthenticated {
                Button("Logout") {
                    client.logout()
                    handleAuthentication()
                }
            } else {
                Button("Authenticate") {
                    showAuthentication = true
                }
            }
        }
        .task {
            handleAuthentication()
        }
        .sheet(isPresented: $showAuthentication) {
            LoginView()
        }
    }
    
    @ViewBuilder
    func DismissButton() -> some View {
        Button {
            handleAuthentication()
        } label: {
            Image(systemName: "xmark")
        }
        .buttonStyle(.glassProminent)
    }
    
    @ViewBuilder
    func WebContent() -> some View {
        client.loginView { _ in
            await handleAuthentication()
        }
    }
    
    #if os(macOS)
    @ViewBuilder
    func LoginView() -> some View {
        VStack {
            HStack {
                Spacer()
                Text("Login")
                Spacer()
                DismissButton()
            }
            WebContent()
        }
        .padding()
        .frame(minWidth: 500.0, minHeight: 500.0)
    }
    #else
    @ViewBuilder
    func LoginView() -> some View {
        NavigationView {
            WebContent()
            .navigationTitle("Login")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItemGroup(placement: .primaryAction) {
                    DismissButton()
                }
            }
        }
    }
    #endif
    
    @MainActor
    func handleAuthentication() {
        self.showAuthentication = false
        isAuthenticated = client.isAuthenticated
    }
}

#if os(macOS)
enum TitleDisplayMode: Sendable {
    case inline
}

extension View {
    @ViewBuilder
    func navigationBarTitleDisplayMode(
        _ mode: TitleDisplayMode
    ) -> some View {
        self
    }
}
#endif

#Preview {
    AuthenticationButton()
}
