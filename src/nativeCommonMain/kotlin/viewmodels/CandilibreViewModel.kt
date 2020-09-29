package viewmodels

import constants.City
import constants.Department
import constants.PARIS_TIMEZONE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import logging.Logger
import services.api.CandilibApi
import services.api.client.CandilibreClientBadTokenException
import services.booking.BookingService
import services.user.UserService
import services.user.errors.UnknownTokenFormatException
import ui.UIStrings.candilibTokenErrorLabel
import ui.UIStrings.logAreaInitialText
import ui.UIStrings.startSearchJobMessage
import ui.UIStrings.stoppingCurrentJobLabel
import ui.UIStrings.unknownErrorLabel
import ui.UIStrings.userIdExtractionFromTokenErrorLabel
import ui.model.UIDepartmentData
import ui.model.UIParamsSectionData

class CandilibreViewModel {
    private val viewModelScope = CoroutineScope(newSingleThreadContext("viewModelThread"))

    private var runningSearchJob = ConflatedBroadcastChannel<Job?>(null)

    val loggingAreaContent = Logger.logLines
    val paramsSectionData = City.values().groupBy { it.department }.map {
        UIDepartmentData(it.key.serverName, it.value.map { city -> city.serverName })
    }.let { UIParamsSectionData(it) }

    init {
        Logger.log(logAreaInitialText)
    }

    fun onStartSearchClicked(magicLink: String, citiesLabels: List<String>, minDateString: String) {
        val cities = convertToCities(citiesLabels)
        val minDate = convertToMinInstant(minDateString)

        runningSearchJob.value.let {
            Logger.log(stoppingCurrentJobLabel)
            it?.cancel()
        }
        Logger.log(startSearchJobMessage(minDateString, citiesLabels.toString()))
        viewModelScope.launch {
            runWithErrorHandling { BookingService().tryBooking(UserService.getToken(magicLink), cities, minDate) }
        }.also(runningSearchJob::offer)
    }

    private inline fun runWithErrorHandling(block: () -> Unit) {
        try {
            block()
        } catch (e: CandilibreClientBadTokenException) {
            Logger.log(candilibTokenErrorLabel)
        } catch (e: UnknownTokenFormatException) {
            Logger.log(userIdExtractionFromTokenErrorLabel)
        } catch (e: Throwable) {
            Logger.log(unknownErrorLabel(e.toString()))
        }
    }

    private fun convertToMinInstant(minDateString: String): Instant = minDateString
        .split("/")
        .let { LocalDateTime("20${it[2]}".toInt(), it[0].toInt(), it[1].toInt(), 0, 0, 0, 0) }
        .toInstant(PARIS_TIMEZONE)

    private fun convertToCities(citiesLabels: List<String>): List<City> = citiesLabels
        .mapNotNull { cityLabel -> City.values().find { cityLabel == it.serverName } }

    private suspend fun convertToUIDepartmentData(token: String, department: Department) = UIDepartmentData(
        department.serverName,
        CandilibApi.getCentres(token, department.serverName).map { it.data!!.name!! }
    )
}