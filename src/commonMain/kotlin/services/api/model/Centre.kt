package services.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Centre(
    val count: Int? = null,
    @SerialName("centre") val data: CentreData? = null
)