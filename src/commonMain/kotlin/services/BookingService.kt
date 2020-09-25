package services

import api.model.BookingResult
import constants.City
import constants.PARIS_TIMEZONE
import helpers.BookingHelper
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime
import logging.Logger
import kotlin.coroutines.coroutineContext
import kotlin.math.abs
import kotlin.random.Random

class BookingService {
    private val randomNumberGenerator = Random(Clock.System.now().hashCode())

    private val minutesRangeAroundMidday = 7L
    private val millisecondsStepDuringRushHour = 1000L // 1sec
    private val millisecondsStepDuringLazyHour = 300000L // 5min
    private val millisecondsRandomDeltaDuringRushHour = 100L // 1/10sec
    private val millisecondsRandomDeltaDuringLazyHour = 60000L // 1min

    suspend fun tryBooking(logger: Logger, token: String, cities: List<City>, minDate: Instant): BookingResult {
        coroutineContext.ensureActive()
        val result = BookingHelper.bookASlot(logger, token, cities, minDate)
        return if (result != null && result.success == true) {
            result
        } else {
            if (result != null) logger.logFailedResult(result)
            // Retry after some delay in case of failure
            val waitingTime = getWaitingTime()
            logger.log("Nouvel essai dans ${waitingTime / 1000f} secondes")
            delay(waitingTime)
            tryBooking(logger, token, cities, minDate)
        }
    }

    private fun Logger.logFailedResult(result: BookingResult) {
        val logLine = "Echec de la réservation :(\nréponse de Candilib : $result"
        log(logLine)
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