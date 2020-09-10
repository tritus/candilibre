package services

import api.model.BookingResult
import constants.PARIS_TIMEZONE
import helpers.BookingHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.datetime.*
import kotlin.math.abs
import kotlin.random.Random

class BookingService {
    private val randomNumberGenerator = Random(Clock.System.now().hashCode())

    private val minutesRangeAroundMidday = 5L
    private val millisecondsStepDuringRushHour = 1000L // 1sec
    private val millisecondsStepDuringLazyHour = 300000L // 5min
    private val millisecondsRandomDeltaDuringRushHour = 100L // 1/10sec
    private val millisecondsRandomDeltaDuringLazyHour = 60000L // 1min

    suspend fun tryBooking(): BookingResult {
        val result = BookingHelper.bookASlot()
        return if (result != null && result.success) {
            result
        } else {
            // Retry after some delay in case of failure
            val waitingTime = getWaitingTime()
            println("Retry in $waitingTime millisec")
            delay(waitingTime)
            tryBooking()
        }
    }

    private fun getWaitingTime(): Long {
        return if (itIsRushHour()) {
            getWaitingTime(millisecondsStepDuringRushHour, millisecondsRandomDeltaDuringRushHour)
        } else {
            getWaitingTime(millisecondsStepDuringLazyHour, millisecondsRandomDeltaDuringLazyHour)
        }
    }

    private fun getWaitingTime(step: Long, randomDelta: Long): Long {
        val delta = randomNumberGenerator.nextLong(-randomDelta / 2, randomDelta / 2)
        return step + delta
    }

    private fun itIsRushHour(): Boolean {
        val now = Clock.System.now().toLocalDateTime(PARIS_TIMEZONE)
        val nowMinutesOfDay = now.hour * 60 + now.minute
        val middayMinutesOfDay = 12 * 60
        val minutesToMidday = abs(middayMinutesOfDay - nowMinutesOfDay)
        return minutesToMidday < minutesRangeAroundMidday
    }
}