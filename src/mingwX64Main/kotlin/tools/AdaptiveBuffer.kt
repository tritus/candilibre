package tools

/**
 * AdaptiveBuffer is inspired by JetBrains' implementation of
 * native ByteArrayOutputStream known from JVM.
 */
@Suppress("MemberVisibilityCanBePrivate")
class AdaptiveBuffer {

    private var buf: ByteArray
    private var count: Int = 0


    init {
        buf = ByteArray(32)
    }


    private fun arraycopy(src: ByteArray, srcPos: Int, dst: ByteArray, dstPos: Int, len: Int) {
        for (i in 0 until len) {
            dst[dstPos + i] = src[srcPos + i]
        }
    }


    private fun expand(i: Int) {
        if (count + i <= buf.size) {
            return
        }

        val newbuf = ByteArray((count + i) * 2)
        arraycopy(buf, 0, newbuf, 0, count)
        buf = newbuf
    }


    fun size(): Int {
        return count
    }


    fun toByteArray(): ByteArray {
        val newArray = ByteArray(count)
        arraycopy(buf, 0, newArray, 0, count)
        return newArray
    }


    fun write(buffer: ByteArray) = write(buffer, 0, buffer.size)


    fun write(buffer: ByteArray, offset: Int, count: Int) {

        // Avoid int overflow
        if (offset < 0 || offset > buffer.size || count < 0 || count > buffer.size - offset) {
            throw IndexOutOfBoundsException()
        }

        if (count == 0) {
            return
        }

        // Expand if necessary.
        expand(count)
        arraycopy(buffer, offset, buf, this.count, count)
        this.count += count
    }

}
