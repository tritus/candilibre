package wininet

class Response(
    val code: Int,
    val headers: ByteArray,
    val data: ByteArray
)