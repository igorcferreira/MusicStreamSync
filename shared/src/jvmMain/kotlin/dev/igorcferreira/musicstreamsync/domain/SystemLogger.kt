package dev.igorcferreira.musicstreamsync.domain

import org.slf4j.LoggerFactory

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object SystemLogger {
    actual fun info(
        tag: String,
        message: String,
    ) {
        LoggerFactory.getLogger(tag).info(message)
    }

    actual fun debug(
        tag: String,
        message: String,
    ) {
        LoggerFactory.getLogger(tag).debug(message)
    }

    actual fun error(
        tag: String,
        message: String,
        error: Throwable?,
    ) {
        LoggerFactory.getLogger(tag).error(message, error)
    }
}
