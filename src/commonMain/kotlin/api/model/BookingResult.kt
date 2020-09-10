package api.model

import kotlinx.serialization.Serializable

@Serializable
data class BookingResult(
    val success: Boolean,
    val message: String,
    val statusmail: Boolean,
    val dateAfterBook: String,
    val reservation: ReservationResult
)