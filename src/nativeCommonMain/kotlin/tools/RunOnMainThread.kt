package tools

expect suspend fun runOnMainThread(block: suspend () -> Unit)