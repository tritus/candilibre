package tools

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.newSingleThreadContext

actual fun newDispatcher(name: String): CoroutineDispatcher = newSingleThreadContext("name")