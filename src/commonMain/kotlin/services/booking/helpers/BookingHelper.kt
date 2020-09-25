package services.booking.helpers

import constants.City
import constants.Department
import constants.PARIS_TIMEZONE
import kotlinx.datetime.*
import logging.Logger
import services.api.CandilibApi
import services.api.model.BookingResult
import services.api.model.Centre
import services.api.model.Place

internal object BookingHelper {

    suspend fun bookASlot(logger: Logger, token: String, cities: List<City>, minDate: Instant): BookingResult? =
        Department.values()
            .map { findSlotsInDep(token, it) }
            .flatten()
            .takeIf { it.isNotEmpty() }
            ?.also { slots -> logger.logAvailableSlots(slots) }
            ?.filter { slot -> slot.centreName in cities.map { it.serverName } && slot.date > minDate }
            ?.let { bookAnAvailableSlot(token, it) }
            .also { if (it == null) logger.logFailed() }

    private fun Logger.logFailed() {
        val logLine = "${Clock.System.now()} : Pas de places disponibles pour le moment"
        log(logLine)
    }

    private fun Logger.logAvailableSlots(slots: List<Slot>) {
        val slotsList = slots
            .joinToString("\n") { "${it.date.toLocalDateTime(PARIS_TIMEZONE)} in ${it.centreName}" }
        val logLine = "${Clock.System.now()} : Des places sont disponibles : [\n$slotsList\n]"
        log(logLine)
    }

    private suspend fun bookAnAvailableSlot(token: String, slotList: List<Slot>): BookingResult? {
        slotList
            .shuffled()
            .sortedBy { localDateOf(it) }
            .forEach { slot ->
                val result = bookASlot(token, slot)
                if (result?.success == true) return result
            }
        return null
    }

    private fun localDateOf(slot: Slot): LocalDate = slot.date.toLocalDateTime(PARIS_TIMEZONE).date

    private suspend fun bookASlot(token: String, slot: Slot): BookingResult? = toPlace(slot)
        .let { CandilibApi.bookPlace(token, it) }
        .body

    private suspend fun findSlotsInDep(token: String, department: Department): List<Slot> =
        CandilibApi.getCentres(token, department.serverName).body
            .filter { it.count != null && it.count > 0 }
            .map { findSlotsInCentre(token, it) }
            .flatten()

    private suspend fun findSlotsInCentre(token: String, centre: Centre): List<Slot> = requireNotNull(
        centre.data?.id
    ) { "centre id is required to look for places in the centre" }
        .let { CandilibApi.getPlacesForCentre(token, it).body }
        .map { toSlot(it, centre) }

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