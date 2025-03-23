package dev.igorcferreira.musicstreamsync.domain

import dev.igorcferreira.os.logger.OSLogger
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object SystemLogger {
    private const val SUBSYSTEM = "dev.igorcferreira.musicstreamsync"

    actual fun info(tag: String, message: String) {
        val logger = OSLogger(SUBSYSTEM, tag)
        logger.infoWithMessage(message)
    }

    actual fun debug(tag: String, message: String) {
        val logger = OSLogger(SUBSYSTEM, tag)
        logger.debugWithMessage(message)
    }

    actual fun error(tag: String, message: String, error: Throwable?) {
        val logger = OSLogger(SUBSYSTEM, tag)
        val formatted = if (error == null) {
            message
        } else {
            "$message\n${error.stackTraceToString()}"
        }
        logger.errorWithMessage(formatted)
    }
}
