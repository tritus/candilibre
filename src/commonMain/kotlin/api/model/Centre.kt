package api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Centre(
    val count: Int,
    @SerialName("centre") val data: CentreData
)