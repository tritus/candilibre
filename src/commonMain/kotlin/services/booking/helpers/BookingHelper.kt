package services.booking.helpers

import constants.City
import constants.PARIS_TIMEZONE
import kotlinx.datetime.*
import logging.Logger
import services.api.CandilibApi
import services.api.model.BookingResult
import services.api.model.Place

internal object BookingHelper {

    suspend fun bookASlot(token: String, cities: List<City>, minDate: Instant): BookingResult? = cities
        .map { findSlotsInCity(token, it) }
        .flatten()
        .takeIf { it.isNotEmpty() }
        ?.also { slots -> Logger.logAvailableSlots(slots) }
        ?.filter { slot -> slot.centreName in cities.map { it.serverName } && slot.date > minDate }
        ?.let { bookAnAvailableSlot(token, it) }
        .also { if (it == null) Logger.logFailed() }

    private fun Logger.logFailed() {
        val logLine = "Pas de places disponibles pour le moment"
        log(logLine)
    }

    private fun Logger.logAvailableSlots(slots: List<Slot>) {
        val slotsList = slots
            .joinToString("\n") { "${it.date.toLocalDateTime(PARIS_TIMEZONE)} in ${it.centreName}" }
        val logLine = "Des places sont disponibles : [\n$slotsList\n]"
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

    private suspend fun findSlotsInCity(token: String, city: City): List<Slot> = CandilibApi
        .getPlacesForCentre(token, city.department.serverName, city.serverName)
        .map { toSlot(it, city) }

    private fun toSlot(dateString: String, city: City) = Slot(
        parse(dateString),
        dateString,
        city.serverName
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