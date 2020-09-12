package api.model

import kotlinx.serialization.Serializable

@Serializable
data class ReservationResult(
    val date: String? = null,
    val centre: String? = null,
    val departement: String? = null,
    val isBooked: Boolean? = null
)