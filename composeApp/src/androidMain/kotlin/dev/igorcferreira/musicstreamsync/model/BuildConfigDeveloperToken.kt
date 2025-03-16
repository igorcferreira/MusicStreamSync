package dev.igorcferreira.musicstreamsync.model

import dev.igorcferreira.musicstreamsync.BuildConfig

class BuildConfigDeveloperToken : DeveloperToken("", "", "") {
    override val teamId: String
        get() = BuildConfig.TEAM_ID
    override val keyId: String
        get() = BuildConfig.KEY_ID
    override val privateKey: String
        get() = BuildConfig.PRIVATE_KEY
}