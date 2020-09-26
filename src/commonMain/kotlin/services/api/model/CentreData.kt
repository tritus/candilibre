package services.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CentreData(
    @SerialName("_id") val id: String? = null,
    @SerialName("nom") val name: String? = null,
    @SerialName("geoDepartement") val departmentNumber: String? = null
)