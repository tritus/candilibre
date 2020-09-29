package logging

import constants.PARIS_TIMEZONE
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.toLocalDateTime

object Logger {
    private const val logSeparator = "\n\n"
    private val logChannel = ConflatedBroadcastChannel("")
    val logLines = logChannel.asFlow()

    fun log(message: String) {
        val messageWithHour = "${humanizedNow()} - $message"
        val newLog = logSeparator + messageWithHour
        println(messageWithHour)
        logChannel.offer(newLog)
    }

    private fun humanizedNow(): String {
        val nowInFrance = Clock.System.now().toLocalDateTime(PARIS_TIMEZONE)
        return "${nowInFrance.hour}:${nowInFrance.minute}:${nowInFrance.second}"
    }
}
