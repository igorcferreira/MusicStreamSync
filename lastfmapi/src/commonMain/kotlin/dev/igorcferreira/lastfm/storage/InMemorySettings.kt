package dev.igorcferreira.lastfm.storage

import com.russhwolf.settings.Settings
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Trivial map-backed [Settings], used to keep an imported [dev.igorcferreira.lastfm.model.Session]
 * scoped to a single [dev.igorcferreira.lastfm.LastFMClient] instance instead of the
 * process-global no-arg [Settings]. Not persisted.
 *
 * Individual reads and writes are thread-safe (copy-on-write over an atomic map
 * reference), so concurrent readers never observe a corrupted map. Multi-key values —
 * a serialized session spans several keys — are still not replaced atomically as a
 * group: callers that swap sessions while requests are in flight (e.g. a token-refresh
 * endpoint) should serialize those updates per client.
 */
@OptIn(ExperimentalAtomicApi::class)
internal class InMemorySettings : Settings {
    private val storage = AtomicReference<Map<String, Any>>(emptyMap())

    private fun mutate(transform: (Map<String, Any>) -> Map<String, Any>) {
        while (true) {
            val current = storage.load()
            if (storage.compareAndSet(current, transform(current))) {
                return
            }
        }
    }

    override val keys: Set<String>
        get() = storage.load().keys
    override val size: Int
        get() = storage.load().size

    override fun clear() = mutate { emptyMap() }

    override fun remove(key: String) = mutate { it - key }

    override fun hasKey(key: String): Boolean = storage.load().containsKey(key)

    override fun putInt(
        key: String,
        value: Int,
    ) = mutate { it + (key to value) }

    override fun getInt(
        key: String,
        defaultValue: Int,
    ): Int = getIntOrNull(key) ?: defaultValue

    override fun getIntOrNull(key: String): Int? = storage.load()[key] as? Int

    override fun putLong(
        key: String,
        value: Long,
    ) = mutate { it + (key to value) }

    override fun getLong(
        key: String,
        defaultValue: Long,
    ): Long = getLongOrNull(key) ?: defaultValue

    override fun getLongOrNull(key: String): Long? = storage.load()[key] as? Long

    override fun putString(
        key: String,
        value: String,
    ) = mutate { it + (key to value) }

    override fun getString(
        key: String,
        defaultValue: String,
    ): String = getStringOrNull(key) ?: defaultValue

    override fun getStringOrNull(key: String): String? = storage.load()[key] as? String

    override fun putFloat(
        key: String,
        value: Float,
    ) = mutate { it + (key to value) }

    override fun getFloat(
        key: String,
        defaultValue: Float,
    ): Float = getFloatOrNull(key) ?: defaultValue

    override fun getFloatOrNull(key: String): Float? = storage.load()[key] as? Float

    override fun putDouble(
        key: String,
        value: Double,
    ) = mutate { it + (key to value) }

    override fun getDouble(
        key: String,
        defaultValue: Double,
    ): Double = getDoubleOrNull(key) ?: defaultValue

    override fun getDoubleOrNull(key: String): Double? = storage.load()[key] as? Double

    override fun putBoolean(
        key: String,
        value: Boolean,
    ) = mutate { it + (key to value) }

    override fun getBoolean(
        key: String,
        defaultValue: Boolean,
    ): Boolean = getBooleanOrNull(key) ?: defaultValue

    override fun getBooleanOrNull(key: String): Boolean? = storage.load()[key] as? Boolean
}
