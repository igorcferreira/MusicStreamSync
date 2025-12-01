package dev.igorcferreira.musicstreamsync.domain

import android.util.Log

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object SystemLogger {
    actual fun info(tag: String, message: String) { Log.i(tag, message) }
    actual fun debug(tag: String, message: String) { Log.d(tag, message) }
    actual fun error(tag: String, message: String, error: Throwable?) {
        Log.e(tag, message, error)
    }
}
