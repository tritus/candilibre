package api.model

import kotlinx.serialization.Serializable

@Serializable
data class ReservationResult(
    val date: String,
    val centre: String,
    val departement: String,
    val isBooked: Boolean
)