package api

import api.model.BookingResult
import api.model.Centre
import api.model.Place
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal object CandilibApi {
    private val scheme = "http"
    private val appHost = "localhost:8000"
    private val apiPath = "api/v2"
    private val appJWTToken =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjVmNTc5M2FkOGJlODczNGQzMzJiYmY2NiIsImxldmVsIjowLCJpYXQiOjE1OTk1OTk4ODIsImV4cCI6MTU5OTg1OTA4Mn0.IyuVMGeDS08c15gusF8JeTdC4O8krwdSb_8Jf92N99E"

    // begin API endpoints

    suspend fun getCentres(depNumber: String): List<Centre>? = get("candidat/centres", "departement" to depNumber)

    suspend fun getPlacesForCentre(centreId: String): List<String>? = get("candidat/places/$centreId")

    suspend fun bookPlace(place: Place): BookingResult? = patch("candidat/places", Json.encodeToString(place))

    // end API endpoints

    private suspend inline fun <reified ExpectedResponse> get(
        endpoint: String,
        vararg urlParams: Pair<String, String>
    ): ExpectedResponse? {
        return try {
            getFromKtor<ExpectedResponse>(endpoint, *urlParams).also { println("API CALL SUCCESS on $endpoint : $it") }
        } catch (e: Throwable) {
            println("API CALL ERROR on $endpoint : ${e.message}")
            null
        }
    }

    private suspend inline fun <reified ExpectedResponse> patch(
        endpoint: String,
        requestBody: String
    ): ExpectedResponse? {
        return try {
            patchFromKtor<ExpectedResponse>(endpoint, requestBody)
                .also { println("API CALL SUCCESS on $endpoint : $it") }
        } catch (e: Throwable) {
            println("API CALL ERROR on $endpoint : ${e.message}")
            null
        }
    }

    private suspend inline fun <reified ExpectedResponse> getFromKtor(
        endpoint: String,
        vararg urlParams: Pair<String, String>
    ): ExpectedResponse {
        return HttpClient().get<String> {
            val params = urlParams.joinToString("&") { "${it.first}=${it.second}" }
            url("$scheme://$appHost/$apiPath/$endpoint?$params")
            header("accept", "application/json")
            header("Authorization", "Bearer $appJWTToken")
        }.let {
            Json { ignoreUnknownKeys = true }.decodeFromString(it)
        }
    }

    private suspend inline fun <reified ExpectedResponse> patchFromKtor(
        endpoint: String,
        requestBody: String
    ): ExpectedResponse {
        return HttpClient().patch<String> {
            url("$scheme://$appHost/$apiPath/$endpoint")
            header("accept", "application/json")
            header("Authorization", "Bearer $appJWTToken")
            body = requestBody
        }.let {
            Json { ignoreUnknownKeys = true }.decodeFromString(it)
        }
    }
}