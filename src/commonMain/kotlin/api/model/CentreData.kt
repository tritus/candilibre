package api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CentreData(
    @SerialName("_id") val id: String,
    @SerialName("nom") val name: String,
    @SerialName("geoDepartement") val departmentNumber: String
)