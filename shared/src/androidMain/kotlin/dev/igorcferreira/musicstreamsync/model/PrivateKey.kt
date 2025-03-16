package dev.igorcferreira.musicstreamsync.model

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual typealias PrivateKey = ByteArray

actual fun String.extractPrivateKey(): PrivateKey = encodeToByteArray()