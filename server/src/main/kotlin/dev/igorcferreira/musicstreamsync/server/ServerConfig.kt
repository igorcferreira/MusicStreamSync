package dev.igorcferreira.musicstreamsync.server

/**
 * Server configuration, sourced from environment variables:
 *
 * - `PORT` — HTTP port (default [DEFAULT_PORT]).
 * - `MONGODB_URI` — MongoDB connection string (default [DEFAULT_MONGODB_URI]).
 * - `SYNC_SHARED_SECRET` — bearer secret the apps use to call the token API
 *   (TASK_4 onwards). **No default**: startup fails fast when unset.
 * - `SYNC_INTERVAL_MINUTES` — sync loop interval (default
 *   [DEFAULT_SYNC_INTERVAL_MINUTES]; consumed by TASK_6).
 */
data class ServerConfig(
    val port: Int,
    val mongodbUri: String,
    val syncSharedSecret: String,
    val syncIntervalMinutes: Int,
) {
    companion object {
        const val DEFAULT_PORT = 8080
        const val DEFAULT_MONGODB_URI = "mongodb://mongodb:27017/musicstreamsync"
        const val DEFAULT_SYNC_INTERVAL_MINUTES = 5
        const val DEFAULT_DATABASE_NAME = "musicstreamsync"
        private const val MAX_PORT = 65535

        fun fromEnvironment(env: (String) -> String? = System::getenv): ServerConfig {
            val secret =
                env("SYNC_SHARED_SECRET")?.takeIf { it.isNotBlank() }
                    ?: error(
                        "SYNC_SHARED_SECRET environment variable must be set: it is the shared " +
                            "secret the mobile apps use to authenticate against the token API.",
                    )
            return ServerConfig(
                port =
                    positiveInt("PORT", env("PORT"), DEFAULT_PORT).also { port ->
                        check(port <= MAX_PORT) { "PORT must be in 1..$MAX_PORT, was: $port" }
                    },
                mongodbUri = env("MONGODB_URI")?.takeIf { it.isNotBlank() } ?: DEFAULT_MONGODB_URI,
                syncSharedSecret = secret,
                syncIntervalMinutes =
                    positiveInt(
                        "SYNC_INTERVAL_MINUTES",
                        env("SYNC_INTERVAL_MINUTES"),
                        DEFAULT_SYNC_INTERVAL_MINUTES,
                    ),
            )
        }

        // Absent/blank falls back to the default; a *present* value that does not parse
        // to a positive integer is an operator error and fails startup, mirroring the
        // fail-fast handling of SYNC_SHARED_SECRET.
        private fun positiveInt(
            name: String,
            raw: String?,
            default: Int,
        ): Int {
            val value = raw?.takeIf { it.isNotBlank() }?.trim() ?: return default
            val parsed = value.toIntOrNull() ?: error("$name must be an integer, was: \"$value\"")
            check(parsed > 0) { "$name must be a positive integer, was: $parsed" }
            return parsed
        }
    }
}
