import services.BookingService

internal object App {
    suspend fun runApp() {
        val service = BookingService()
        try {
            service.bookASlot()
        } finally {
            service.stop()
        }
    }
}