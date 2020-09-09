import api.CandilibApi
import kotlinx.coroutines.*
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.datetime.*
import kotlin.math.abs
import kotlin.random.Random

internal class Worker {
    private val randomNumberGenerator = Random(Clock.System.now().hashCode())

    private val minutesRangeAroundMidday = 5L
    private val millisecondsStepDuringRushHour = 1000L // 1sec
    private val millisecondsStepDuringLazyHour = 300000L // 5min
    private val millisecondsRandomDeltaDuringRushHour = 100L // 1/10sec
    private val millisecondsRandomDeltaDuringLazyHour = 60000L // 1min

    private var shouldStop = false

    @OptIn(InternalCoroutinesApi::class)
    suspend fun doWork() {
        shouldStop = false
        while (!shouldStop && isActive) {
            fetch()
            delay(getNextWaitingTime())
        }
    }

    fun stop() {
        shouldStop = true
    }

    private fun getNextWaitingTime(): Long {
        return if (itIsRushHour()) {
            getNextWaitingTime(millisecondsStepDuringRushHour, millisecondsRandomDeltaDuringRushHour)
        } else {
            getNextWaitingTime(millisecondsStepDuringLazyHour, millisecondsRandomDeltaDuringLazyHour)
        }
    }

    private fun getNextWaitingTime(step: Long, randomDelta: Long): Long {
        val delta = randomNumberGenerator.nextLong(-randomDelta / 2, randomDelta / 2)
        return step + delta
    }

    private fun itIsRushHour(): Boolean {
        val now = Clock.System.now().toLocalDateTime(TimeZone.of("Europe/Paris"))
        val nowMinutesOfDay = now.hour * 60 + now.minute
        val middayMinutesOfDay = 12 * 60
        val minutesToMidday = abs(middayMinutesOfDay - nowMinutesOfDay)
        return minutesToMidday < minutesRangeAroundMidday
    }

    private suspend fun fetch() {
        println("get centres")
        CandilibApi.getCentres("93")
            ?.sumBy { it.count }
            ?.let { println("Available slots count : $it") }
    }
}