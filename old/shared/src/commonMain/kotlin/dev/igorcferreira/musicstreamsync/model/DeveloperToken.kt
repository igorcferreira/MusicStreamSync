package dev.igorcferreira.musicstreamsync.model

import com.arkanakeys.ArkanaKeys

open class DeveloperToken {
    open val teamId: String
        get() = ArkanaKeys.Global.teamId
    open val keyId: String
        get() = ArkanaKeys.Global.keyId
    open val privateKey: String
        get() = ArkanaKeys.Global.privateKey
}
