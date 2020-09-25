package api

import api.client.buildClient
import api.model.BookingResult
import api.model.Centre
import api.model.Place
import logging.Logger

internal object CandilibApi {
    private fun httpClient(token: String) = buildClient(
        scheme = "https",
        appHost = "beta.interieur.gouv.fr/candilib",
        apiPath = "api/v2",
        appJWTToken = token
    )

    suspend fun getCentres(token: String, depNumber: String): List<Centre> =
        httpClient(token).get("candidat/centres", "departement" to depNumber)

    suspend fun getPlacesForCentre(token: String, centreId: String): List<String> =
        httpClient(token).get("candidat/places/$centreId")

    suspend fun bookPlace(token: String, place: Place): BookingResult =
        httpClient(token).patch("candidat/places", place)
}