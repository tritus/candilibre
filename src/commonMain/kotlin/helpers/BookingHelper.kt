package helpers

import api.CandilibApi
import api.model.BookingResult
import api.model.Centre
import api.model.Place
import constants.City
import constants.PARIS_TIMEZONE
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import logging.logInFile

internal object BookingHelper {

    suspend fun bookASlot(cities: List<City>, minDate: Instant): BookingResult? = cities
        .map { findSlotsInCity(it) }
        .flatten()
        .takeIf { it.isNotEmpty() }
        ?.also { slots -> logAvailableSlots(slots) }
        ?.filter { it.date > minDate }
        ?.minByOrNull { it.date }
        ?.let { book(it) }

    private fun logAvailableSlots(slots: List<Slot>) {
        val slotsList = slots
            .joinToString("\n") { "${it.date.toLocalDateTime(PARIS_TIMEZONE)} in ${it.centreName}" }
        val logLine = "${Clock.System.now()} : SLOTS AVAILABLE [\n$slotsList\n]"
        println(logLine)
        logInFile(logLine)
    }

    private suspend fun book(slot: Slot): BookingResult? = toPlace(slot)
        .let { CandilibApi.bookPlace(it) }

    private suspend fun findSlotsInCity(city: City): List<Slot> = CandilibApi.getCentres(city.dep)
        ?.find { it.data?.name == city.serverName }
        ?.takeIf { it.count != null && it.count > 0 }
        ?.let { findSlotsInCentre(it) }
        ?: emptyList()

    private suspend fun findSlotsInCentre(centre: Centre): List<Slot>? = requireNotNull(
        centre.data?.id
    ) { "centre id is required to look for places in the centre" }
        .let { CandilibApi.getPlacesForCentre(it) }
        ?.map { toSlot(it, centre) }

    private fun toSlot(dateString: String, centre: Centre) = Slot(
        parse(dateString),
        requireNotNull(centre.data?.name) { "centre name must not be null to be able to book a slot" }
    )

    private fun toPlace(slot: Slot) = Place(
        slot.centreName,
        slot.date.toString(),
        isAccompanied = true,
        hasDualControlCar = true
    )

    private fun parse(dateString: String): Instant = dateString
        .dropLast(6)
        .toLocalDateTime()
        .toInstant(timeZoneOf(dateString))

    private fun timeZoneOf(dateString: String): TimeZone = when (dateString.takeLast(6)) {
        "+01:00" -> PARIS_TIMEZONE
        else -> {
            println("Error while parsing timezone of date $dateString, falling back on UTC")
            TimeZone.UTC
        }
    }

    private data class Slot(val date: Instant, val centreName: String)
}