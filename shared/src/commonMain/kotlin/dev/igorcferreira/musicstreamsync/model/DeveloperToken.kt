package dev.igorcferreira.musicstreamsync.model

open class DeveloperToken(
    open val teamId: String,
    open val keyId: String,
    open val privateKey: String
)

expect fun String.extractPrivateKey(): PrivateKey
