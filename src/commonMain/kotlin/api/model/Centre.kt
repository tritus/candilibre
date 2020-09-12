package api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Centre(
    val count: Int? = null,
    @SerialName("centre") val data: CentreData? = null
)