import api.model.BookingResult
import constants.City
import constants.PARIS_TIMEZONE
import kotlinx.coroutines.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import logging.logInFile
import services.BookingService

fun main(): Unit = runBlocking {
    val preferedCities = listOf(
        City.BOBIGNY,
        City.GENNEVILLIERS,
        City.MAISONS_ALFORT,
        City.RUNGIS,
        City.MASSY,
        City.NOISY_LE_GRAND,
        City.ROSNY_SOUS_BOIS,
        City.SAINT_CLOUD,
        City.SAINT_LEU_LA_FORET,
        City.VELIZY_VILLACOUBLAY
    )

    val minDate = LocalDateTime(2020, 9, 27, 0, 0, 0, 0).toInstant(PARIS_TIMEZONE)


    val result = BookingService().tryBooking(preferedCities, minDate)
    logSuccess(result)
}

fun logSuccess(result: BookingResult) {
    val successLog =
        "SUCCESS : booking taken on ${result.reservation.date} in ${result.reservation.centre} (${result.reservation.departement})"
    println(successLog)
    logInFile(successLog)
}
