package dev.igorcferreira.musicstreamsync.model

import platform.Foundation.NSData
import platform.Foundation.create

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual typealias PrivateKey = NSData

actual fun String.extractPrivateKey(): PrivateKey = NSData.create(base64Encoding = this) ?: NSData()