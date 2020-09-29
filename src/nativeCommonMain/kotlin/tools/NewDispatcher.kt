package tools

import kotlinx.coroutines.CoroutineDispatcher

expect fun newDispatcher(name: String): CoroutineDispatcher