//
//  LastFMAuthentication.swift
//  iosApp
//
//  Created by Igor Ferreira on 22/03/2025.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI

struct LastFMAuthentication: View {
    
    enum FieldFocus: Int {
        case username
        case password
    }
    
    private let authenticate: (String, String) async -> Void
    @State private var username: String = ""
    @State private var password: String = ""
    @State private var isAuthenticating: Bool = false
    @FocusState private var focus: FieldFocus?
    
    init(
        authenticate: @escaping (String, String) async -> Void
    ) {
        self.authenticate = authenticate
    }
    
    var body: some View {
        VStack(spacing: 16.0) {
            Text("Authenticate to Last.FM")
                .font(.title)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.bottom, 8.0)
            
            Text("Input Username and Password to scrobble current listening to Last.FM")
                .font(.body)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.bottom, 8.0)
            
            VStack {
                Text("Username:")
                    .font(.caption)
                    .frame(maxWidth: .infinity, alignment: .leading)
                TextField("Username", text: $username)
                    .textContentType(.username)
                    .textFieldStyle(.roundedBorder)
                    .focused($focus, equals: .username)
            }
            
            VStack {
                Text("Password:")
                    .font(.caption)
                    .frame(maxWidth: .infinity, alignment: .leading)
                SecureField("Password", text: $password)
                    .textContentType(.password)
                    .textFieldStyle(.roundedBorder)
                    .focused($focus, equals: .password)
            }
            
            if isAuthenticating {
                ProgressView()
            } else {
                Button {
                    action()
                } label: {
                    Text("Authenticate")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
                .padding(.top, 16.0)
            }
            Spacer()
        }
        .onFirstTask { focus = .username }
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
