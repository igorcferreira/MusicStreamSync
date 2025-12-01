package dev.igorcferreira.musicstreamsync.domain

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object SystemLogger {
    fun info(tag: String, message: String)
    fun debug(tag: String, message: String)
    fun error(tag: String, message: String, error: Throwable? = null)
}
