package logging

interface Logger {
    fun log(message: String)
    suspend fun read(onMessage: (String) -> Unit)
}
