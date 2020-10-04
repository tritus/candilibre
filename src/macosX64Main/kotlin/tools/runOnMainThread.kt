package tools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun runOnMainThread(block: suspend () -> Unit) = withContext(Dispatchers.Main) { block() }