package helpers

import api.CandilibApi
import api.model.Centre
import constants.PARIS_TIMEZONE
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

internal object BookingHelper {

    private val interestingCities = listOf(
        City("BOBIGNY", "93"),
        City("GENNEVILLIERS", "92"),
        City("MAISONS ALFORT", "94"),
        City("RUNGIS", "94"),
        City("MASSY", "91"),
        City("NOISY LE GRAND", "93"),
        City("ROSNY SOUS BOIS", "93"),
        City("SAINT CLOUD", "92"),
        City("SAINT LEU LA FORET", "95"),
        City("VELIZY VILLACOUBLAY", "78")
    )

    suspend fun tryToBookASlot() = coroutineScope {
        println("tryToBookASlot")
        interestingCities
            .map { async { println("findSlotsInPlace $it"); findSlotsInPlace(it) } }
            .awaitAll()
            .flatten()
            .also {
                val slotsList = it.joinToString("\n") { "${it.date.toLocalDateTime(PARIS_TIMEZONE)} in ${it.centreName}" }
                println("SLOTS AVAILABLE : [\n$slotsList\n]")
            }
            .firstOrNull()
            ?.let { book(it) }
            ?: println("NO SLOT AVAILABLE")
    }

    private fun book(slot: Slot) {
        // TODO : implement booking
    }

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

    private data class City(val name: String, val dep: String)
    private data class Slot(
        val date: Instant,
        val dateString: String, // We keep it to be able to send it back to the server without modifying it.
        val centreName: String,
        val centreId: String
    )
}