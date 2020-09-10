package helpers

import api.CandilibApi
import api.model.BookingResult
import api.model.Centre
import api.model.Place
import constants.City
import constants.PARIS_TIMEZONE
import kotlinx.datetime.*
import logging.logInFile

internal object BookingHelper {

    suspend fun bookASlot(cities: List<City>, minDate: Instant): BookingResult? = cities
        .map { findSlotsInPlace(it) }
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

    private suspend fun book(slot: Slot): BookingResult? = slot
        .toPlace()
        .let { CandilibApi.bookPlace(it) }

    private suspend fun findSlotsInPlace(place: City): List<Slot> = CandilibApi.getCentres(place.dep)
        ?.find { it.data.name == place.name }
        ?.takeIf { it.count > 0 }
        ?.let { findSlotsInCentre(it) }
        ?: emptyList()

    private suspend fun findSlotsInCentre(centre: Centre): List<Slot>? = CandilibApi.getPlacesForCentre(centre.data.id)
        ?.map { toSlot(it, centre) }

    private fun toSlot(dateString: String, centre: Centre) = Slot(
        parse(dateString),
        dateString,
        centre.data.name,
        centre.data.id
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

    private data class Slot(
        val date: Instant,
        val dateString: String, // We keep it to be able to send it back to the server without modifying it.
        val centreName: String,
        val centreId: String
    ) {
        fun toPlace() = Place(
            centreId,
            dateString,
            isAccompanied = true,
            hasDualControlCar = true
        )
    }
}