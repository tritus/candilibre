
import api.model.BookingResult
import constants.City
import constants.PARIS_TIMEZONE
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import logging.logInFile
import services.BookingService

fun main() {
    runBlocking {
        val preferredCities = listOf(
            //City.BOBIGNY,
            //City.GENNEVILLIERS,
            City.MAISONS_ALFORT,
            City.RUNGIS,
            //City.MASSY,
            City.NOISY_LE_GRAND,
            //City.ROSNY_SOUS_BOIS,
            //City.SAINT_CLOUD,
            //City.SAINT_LEU_LA_FORET,
            //City.VELIZY_VILLACOUBLAY
        )

        val minDate = LocalDateTime(2020, 9, 27, 0, 0, 0, 0).toInstant(PARIS_TIMEZONE)


        val result = BookingService().tryBooking(preferredCities, minDate)
        logSuccess(result)
        logResult(result)
    }
}

fun logSuccess(result: BookingResult) {
    val successLog =
        "SUCCESS : booking taken on date ${result.reservation?.date} in centre ${result.reservation?.centre} (dep ${result.reservation?.departement})"
    println(successLog)
    logInFile(successLog)
}

fun logResult(result: BookingResult) {
    val logline = "booking result : $result"
    println(logline)
    logInFile(logline)
}
