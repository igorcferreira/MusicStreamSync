//
//  AuthenticationButton.swift
//  MusicStreamSync
//
//  Created by Igor Ferreira on 07/10/2025.
//
import SwiftUI
import LastFMClient

struct AuthenticationButton: View {
    
    @State private var viewModel: AuthenticationViewModel
    
    init(client: LastFMClient) {
        viewModel = .init(client: client)
    }
    
    var body: some View {
        Group {
            if viewModel.isAuthenticated {
                Button("Logout") {
                    viewModel.logout()
                }
            } else {
                Button("Authenticate") {
                    viewModel.login()
                }
            }
        }
        .sheet(isPresented: $viewModel.showAuthentication) {
            LoginView()
        }
    }
    
    @ViewBuilder
    func DismissButton() -> some View {
        Button {
            viewModel.handleAuthentication()
        } label: {
            Image(systemName: "xmark")
        }
        .buttonStyle(.glassProminent)
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
            viewModel.loginView()
        }
        .padding()
        .frame(minWidth: 500.0, minHeight: 500.0)
    }
    #else
    @ViewBuilder
    func LoginView() -> some View {
        NavigationView {
            viewModel.loginView()
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
    AuthenticationButton(client: LastFMClientKey.defaultValue)
}
