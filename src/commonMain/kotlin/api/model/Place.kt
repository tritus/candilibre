package api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Place(
    @SerialName("id") val centreId: String,
    @SerialName("date") val dateString: String,
    val isAccompanied: Boolean,
    val hasDualControlCar: Boolean
)