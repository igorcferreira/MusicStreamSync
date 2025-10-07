//
//  Endpoint.swift
//  LastFMClient
//
//  Created by Igor Ferreira on 7/10/25.
//
import Foundation

enum HTTPMethod: String, Sendable {
    case post = "POST"
    case get = "GET"
    case put = "PUT"
}

struct Endpoint: Sendable {
    let apiMethod: String
    let method: HTTPMethod
    let parameters: [String: String]
    
    init(
        apiMethod: String,
        method: HTTPMethod,
        parameters: [String : String]
    ) {
        self.apiMethod = apiMethod
        self.method = method
        self.parameters = parameters
    }
}
