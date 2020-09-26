package tools

internal object Base64 {
    private const val BASE64_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    private val RX_BASE64_CLEANR = "[^="+BASE64_SET+"]".toRegex()

    /**
     * Base64 encode a string.
     */
    val String.base64encoded: String get() {
        val pad  = when (this.length % 3) {
            1 -> "=="
            2 -> "="
            else -> ""
        }
        var raw = this
        (1 .. pad.length).forEach { raw += 0.toChar() }
        return StringBuilder().apply {
            (0 until raw.length step 3).forEach {
                val n: Int = (0xFF.and(raw[ it ].toInt()) shl 16) +
                        (0xFF.and(raw[it+1].toInt()) shl  8) +
                        0xFF.and(raw[it+2].toInt())
                listOf<Int>( (n shr 18) and 0x3F,
                    (n shr 12) and 0x3F,
                    (n shr  6) and 0x3F,
                    n      and 0x3F).forEach { append(BASE64_SET[it]) }
            }
        }   .dropLast(pad.length)
            .toString() + pad
    }

    /**
     * Decode a Base64 string.
     */
    val String.base64decoded: String get() {
        return encodeToByteArray().base64decoded.decodeToString()
    }

    /**
     * Decode a Base64 ByteArray.
     */
    val ByteArray.base64decoded: ByteArray get() {
        val table = intArrayOf(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1,
            -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
            -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1)

        val output = mutableListOf<Int>()
        var position = 0
        while (position < this.size) {
            var b: Int
            if (table[this[position].toInt()] != -1) {
                b = table[this[position].toInt()] and 0xFF shl 18
            } else {
                position++
                continue
            }
            var count = 0
            if (position + 1 < this.size && table[this[position + 1].toInt()] != -1) {
                b = b or (table[this[position + 1].toInt()] and 0xFF shl 12)
                count++
            }
            if (position + 2 < this.size && table[this[position + 2].toInt()] != -1) {
                b = b or (table[this[position + 2].toInt()] and 0xFF shl 6)
                count++
            }
            if (position + 3 < this.size && table[this[position + 3].toInt()] != -1) {
                b = b or (table[this[position + 3].toInt()] and 0xFF)
                count++
            }
            while (count > 0) {
                val c = b and 0xFF0000 shr 16
                output.add(c.toChar().toInt())
                b = b shl 8
                count--
            }
            position += 4
        }
        return output.map { it.toByte() }.toByteArray()
    }
}