//
//  NetworkClient.swift
//  LastFMClient
//
//  Created by Igor Ferreira on 7/10/25.
//
import Foundation

struct NetworkClient: Sendable {
    private let session: URLSession
    private let decoder: JSONDecoder
    private let encoder: JSONEncoder
    private let keyHasher: KeyHasher
    private let api: String
    
    private var keyConfiguration: KeyConfiguration? {
        KeyConfiguration.restore()
    }
    
    private var userCredentials: UserSession? {
        UserSession.restore()
    }
    
    init(
        api: String = "https://ws.audioscrobbler.com/2.0",
        keyHasher: KeyHasher,
        session: URLSession = .shared,
        decoder: JSONDecoder = .init(),
        encoder: JSONEncoder = .init()
    ) {
        self.api = api
        self.keyHasher = keyHasher
        self.session = session
        self.decoder = decoder
        self.encoder = encoder
    }
    
    @concurrent
    func perform<T: Decodable>(_ endpoint: Endpoint) async throws -> T {
        return try await self.perform(endpoint, body: Optional<Never>.init(nilLiteral: ()))
    }
    
    @concurrent
    func perform<B, T: Decodable>(_ endpoint: Endpoint, body: B?) async throws -> T where B: Sendable, B: Encodable {
        guard var components = URLComponents(string: api) else {
            throw URLError(URLError.badURL)
        }
        
        components.queryItems = []
        completeParameters(for: endpoint).forEach { key, value in
            components.queryItems?.append(.init(name: key, value: value))
        }
        
        guard let fullURL = components.url else {
            throw URLError(URLError.badURL)
        }
        
        var request = URLRequest(url: fullURL)
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        
        if let body, let data = try? encoder.encode(body) {
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.httpBody = data
        }
        
        #if DEBUG
        print(curl(for: request))
        #endif
        
        let (data, response) = try await session.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse else {
            throw URLError(.badServerResponse)
        }
        
        guard httpResponse.statusCode >= 200 && httpResponse.statusCode < 300 else {
            if let apiError = try? decoder.decode(APIError.self, from: data) {
                throw apiError
            } else {
                throw URLError(.init(rawValue: httpResponse.statusCode))
            }
        }
        
        let decoded = try decoder.decode(T.self, from: data)
        return decoded
    }
    
    private func completeParameters(for endpoint: Endpoint) -> [String: String] {
        var base = endpoint.parameters
        base["method"] = endpoint.apiMethod
        
        guard let configuration = keyConfiguration else {
            return base
        }
        
        base["api_key"] = configuration.apiKey
        
        if let token = userCredentials?.key {
            base["sk"] = token
        }
        
        if let sig = try? keyHasher.hash(secret: configuration.apiSecret, parameter: base) {
            base["api_sig"] = sig
        }
        
        base["format"] = "json"
        
        return base
    }
}

#if DEBUG
extension NetworkClient {
    func curl(for request: URLRequest) -> String {
        guard let url = request.url else { return "" }
        
        var lines = if let method = request.httpMethod {
            ["curl -i -X \(method) \(url)"]
        } else {
            ["curl -i \(url)"]
        }
        
        request.allHTTPHeaderFields?.forEach { key, value in
            lines.append("-H '\(key): \(value)'")
        }
        
        if let body = request.httpBody, let content = String(data: body, encoding: .utf8) {
            lines.append("-d '\(content)'")
        }
        
        let curl = lines.joined(separator: " \\\n")
        return curl
    }
}
#endif
