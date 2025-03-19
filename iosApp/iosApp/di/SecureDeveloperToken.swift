//
//  IOSDeveloperToken.swift
//  iosApp
//
//  Created by Igor Ferreira on 13/03/2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import MusicStream
import ArkanaKeys

class SecureDeveloperToken: DeveloperToken {
    override var teamId: String {
        get { ArkanaKeys.Global().teamId }
        set(newValue) {}
    }
    override var keyId: String {
        get { ArkanaKeys.Global().keyId }
        set(newValue) {}
    }
    override var privateKey: String {
        get {
            let encoded = ArkanaKeys.Global().privateKey
            guard let data = Data(base64Encoded: encoded) else { return "" }
            return String(data: data, encoding: .utf8) ?? ""
        }
        set(newValue) {}
    }
    
    convenience init() {
        self.init(teamId: "", keyId: "", privateKey: "")
    }
}
