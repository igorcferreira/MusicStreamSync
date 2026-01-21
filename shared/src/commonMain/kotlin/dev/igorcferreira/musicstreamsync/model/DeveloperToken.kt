package dev.igorcferreira.musicstreamsync.model

import com.arkanakeys.ArkanaKeys

interface IDeveloperToken {
    val teamId: String
    val keyId: String
    val privateKey: String
}

open class DeveloperToken: IDeveloperToken {
    override val teamId: String
        get() = ArkanaKeys.Global.teamId
    override val keyId: String
        get() = ArkanaKeys.Global.keyId
    override val privateKey: String
        get() = ArkanaKeys.Global.privateKey
}
