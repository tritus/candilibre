import api.CandilibApi
import api.model.Centre
import kotlinx.coroutines.*
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.datetime.*
import kotlin.math.abs
import kotlin.random.Random

internal class Worker {
    private val randomNumberGenerator = Random(Clock.System.now().hashCode())

    private val minutesRangeAroundMidday = 5L
    private val millisecondsStepDuringRushHour = 1000L // 1sec
    private val millisecondsStepDuringLazyHour = 300000L // 5min
    private val millisecondsRandomDeltaDuringRushHour = 100L // 1/10sec
    private val millisecondsRandomDeltaDuringLazyHour = 60000L // 1min

    private var shouldStop = false

    @OptIn(InternalCoroutinesApi::class)
    suspend fun doWork() {
        shouldStop = false
        while (!shouldStop && isActive) {
            SlotBookingHelper.tryToBookASlot()
            delay(getWaitingTime())
        }
    }

    fun stop() {
        shouldStop = true
    }

    private fun getWaitingTime(): Long {
        return if (itIsRushHour()) {
            getWaitingTime(millisecondsStepDuringRushHour, millisecondsRandomDeltaDuringRushHour)
        } else {
            getWaitingTime(millisecondsStepDuringLazyHour, millisecondsRandomDeltaDuringLazyHour)
        }
    }

    private fun getWaitingTime(step: Long, randomDelta: Long): Long {
        val delta = randomNumberGenerator.nextLong(-randomDelta / 2, randomDelta / 2)
        return step + delta
    }

    private fun itIsRushHour(): Boolean {
        val now = Clock.System.now().toLocalDateTime(TimeZone.of("Europe/Paris"))
        val nowMinutesOfDay = now.hour * 60 + now.minute
        val middayMinutesOfDay = 12 * 60
        val minutesToMidday = abs(middayMinutesOfDay - nowMinutesOfDay)
        return minutesToMidday < minutesRangeAroundMidday
    }
}

internal object SlotBookingHelper {

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
            .firstOrNull()
            ?.let { book(it) }
    }

    private fun book(slot: Slot) {
        // TODO : implement booking
        println("booking at ${slot.date} in ${slot.centreName}")
    }

    private suspend fun findSlotsInPlace(place: City): List<Slot> = CandilibApi.getCentres(place.dep)
        ?.find { it.data.name == place.name }
        ?.takeIf { it.count > 0 }
        ?.let { findSlotsInCentre(it) }
        ?: emptyList()

    private suspend fun findSlotsInCentre(centre: Centre): List<Slot>? = CandilibApi.getPlacesForCentre(centre.data.id)
        ?.map { toSlot(it, centre) }

    private fun toSlot(dateString: String, centre: Centre) = Slot(
        LocalDateTime.parse(dateString),
        centre.data.name,
        centre.data.id
    )

    private data class City(val name: String, val dep: String)
    private data class Slot(val date: LocalDateTime, val centreName: String, val centreId: String)
}