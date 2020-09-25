package logging

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow

fun buildLogger() = object : Logger {
    private val channel = Channel<String>(CONFLATED)

    override fun log(message: String) {
        println(message)
        channel.offer(message)
    }

    override suspend fun read(onMessage: (String) -> Unit) {
        channel.receiveAsFlow().collect { onMessage(it) }
    }
}