package android.util

import java.util.Base64

public object Base64 {
    @JvmStatic
    public fun encodeToString(
        input: ByteArray?,
        flags: Int,
    ): String = Base64.getEncoder().encodeToString(input)

    @JvmStatic
    public fun decode(
        str: String?,
        flags: Int,
    ): ByteArray = Base64.getDecoder().decode(str)

    @JvmStatic
    public fun decode(
        input: ByteArray?,
        flags: Int,
    ): ByteArray = Base64.getDecoder().decode(input)
}
