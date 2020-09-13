package helpers

import api.CandilibApi
import api.model.BookingResult
import api.model.Centre
import api.model.Place
import constants.City
import constants.Department
import constants.PARIS_TIMEZONE
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import logging.logInFile

internal object BookingHelper {

    suspend fun bookASlot(cities: List<City>, minDate: Instant): BookingResult? = Department.values()
        .map { findSlotsInDep(it) }
        .flatten()
        .takeIf { it.isNotEmpty() }
        ?.also { slots -> logAvailableSlots(slots) }
        ?.filter { slot -> slot.centreName in cities.map { it.serverName } && slot.date > minDate }
        ?.minByOrNull { it.date }
        ?.let { book(it) }
        .also { if (it == null) logFailed() }

    private fun logFailed() {
        val logLine = "${Clock.System.now()} : NO SLOTS AVAILABLE"
        println(logLine)
        logInFile(logLine)
    }

    private fun logAvailableSlots(slots: List<Slot>) {
        val slotsList = slots
            .joinToString("\n") { "${it.date.toLocalDateTime(PARIS_TIMEZONE)} in ${it.centreName}" }
        val logLine = "${Clock.System.now()} : SLOTS AVAILABLE [\n$slotsList\n]"
        println(logLine)
        logInFile(logLine)
    }

    private suspend fun book(slot: Slot): BookingResult? = toPlace(slot)
        .let { CandilibApi.bookPlace(it) }

    private suspend fun findSlotsInDep(department: Department): List<Slot> = CandilibApi
        .getCentres(department.serverName)
        ?.filter { it.count != null && it.count > 0 }
        ?.map { findSlotsInCentre(it) }
        ?.flatten()
        ?: emptyList()

    private suspend fun findSlotsInCentre(centre: Centre): List<Slot> = requireNotNull(
        centre.data?.id
    ) { "centre id is required to look for places in the centre" }
        .let { CandilibApi.getPlacesForCentre(it) }
        ?.map { toSlot(it, centre) }
        ?: emptyList()

    private fun toSlot(dateString: String, centre: Centre) = Slot(
        parse(dateString),
        dateString,
        requireNotNull(centre.data?.name) { "centre name must not be null to be able to book a slot" }
    )

    private fun toPlace(slot: Slot) = Place(
        slot.centreName,
        slot.serverDate,
        isAccompanied = true,
        hasDualControlCar = true
    )

    private fun parse(dateString: String): Instant = dateString
        .dropLast(6)
        .toLocalDateTime()
        .toInstant(timeZoneOf(dateString))

    private fun timeZoneOf(dateString: String): TimeZone = TimeZone.of(dateString.takeLast(6))

    private data class Slot(val date: Instant, val serverDate: String, val centreName: String)
}