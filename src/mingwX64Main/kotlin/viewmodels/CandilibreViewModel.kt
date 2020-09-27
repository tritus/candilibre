package viewmodels

import constants.City
import constants.Department
import constants.PARIS_TIMEZONE
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import logging.Logger
import services.api.CandilibApi
import services.api.client.CandilibreClientBadTokenException
import services.booking.BookingService
import services.user.UserService
import services.user.errors.UnknownTokenFormatException
import ui.UIStrings
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

    private var runningSearchJob: Job? = null

    val loggingAreaContent = Logger.logLines
    private val magicLinkAreaIsEditableChannel = ConflatedBroadcastChannel(true)
    val magicLinkAreaIsEditable = magicLinkAreaIsEditableChannel.asFlow()
    private val paramsSectionDataChannel = Channel<UIParamsSectionData>(1)
    val paramsSectionData = paramsSectionDataChannel.receiveAsFlow()

    init {
        Logger.log(logAreaInitialText)
    }

    fun onValidateMagicLinkClicked(magicLink: String) {
        viewModelScope.launch {
            runWithErrorHandling {
                magicLinkAreaIsEditableChannel.offer(false)
                val token = UserService.getToken(magicLink)
                Department.values()
                    .map { convertToUIDepartmentData(token, it) }
                    .let { UIParamsSectionData(it) }
                    .let { paramsSectionDataChannel.offer(it) }
            }
        }
    }

    fun onStartSearchClicked(magicLink: String, citiesLabels: List<String>, minDateString: String) {
        val cities = convertToCities(citiesLabels)
        val minDate = convertToMinInstant(minDateString)

        runningSearchJob?.let {
            Logger.log(stoppingCurrentJobLabel)
            it.cancel()
        }
        Logger.log(startSearchJobMessage(minDateString, citiesLabels.toString()))
        runningSearchJob = viewModelScope.launch {
            runWithErrorHandling { BookingService().tryBooking(UserService.getToken(magicLink), cities, minDate) }
        }
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