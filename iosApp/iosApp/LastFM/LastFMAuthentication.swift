//
//  LastFMAuthentication.swift
//  iosApp
//
//  Created by Igor Ferreira on 22/03/2025.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI

struct LastFMAuthentication: View {
    private let authenticate: (String, String) async -> Void
    @State private var username: String = ""
    @State private var password: String = ""
    @State private var isAuthenticating: Bool = false
    
    init(
        authenticate: @escaping (String, String) async -> Void
    ) {
        self.authenticate = authenticate
    }
    
    var body: some View {
        VStack(spacing: 8.0) {
            Text("Authenticate to Last.FM")
                .font(.title)
                .frame(alignment: .leading)
            Text("Input Username and Password to scrobble current listening to Last.FM")
                .font(.body)
                .frame(alignment: .leading)
            TextField("Username", text: $username)
            TextField("Password", text: $password)
            
            if isAuthenticating {
                ProgressView()
            } else {
                Button {
                    action()
                } label: {
                    Text("Authenticate")
                }
            }
        }
        .padding()
    }
    
    func action() { Task {
        isAuthenticating = true
        await authenticate(username, password)
        isAuthenticating = false
    }}
}

#Preview {
    LastFMAuthentication() { _, _ in }
}
