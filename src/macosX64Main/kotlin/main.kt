import kotlinx.coroutines.*
import services.BookingService

fun main() = runBlocking {
    BookingService().tryBooking()
}