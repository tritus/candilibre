package api.model

import kotlinx.serialization.Serializable

@Serializable
data class BookingResult(
    val success: Boolean? = null,
    val message: String? = null,
    val reservation: ReservationResult? = null
)