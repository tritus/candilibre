package tools

actual suspend fun runOnMainThread(block: suspend () -> Unit) = block()