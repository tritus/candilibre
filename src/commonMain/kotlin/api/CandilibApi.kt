package api

import api.engine.httpClientEngine
import api.model.BookingResult
import api.model.Centre
import api.model.Place
import io.ktor.client.HttpClient
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.url
import io.ktor.content.TextContent
import io.ktor.http.ContentType
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

    suspend fun bookPlace(place: Place): BookingResult? = patch("candidat/places", place)

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

    private suspend inline fun <reified ExpectedResponse, reified Body : Any> patch(
        endpoint: String,
        requestBody: Body
    ): ExpectedResponse? {
        return try {
            patchFromKtor<ExpectedResponse, Body>(endpoint, requestBody)
                .also { println("API CALL SUCCESS on $endpoint : $it") }
        } catch (e: Throwable) {
            println("API CALL ERROR on $endpoint : ${e.message}")
            null
        }
    }

    private suspend inline fun <reified ExpectedResponse> getFromKtor(
        endpoint: String,
        vararg urlParams: Pair<String, String>
    ): ExpectedResponse = httpClient().get {
        val params = urlParams.joinToString("&") { "${it.first}=${it.second}" }
        url("$scheme://$appHost/$apiPath/$endpoint?$params")
    }

    private suspend inline fun <reified ExpectedResponse, reified Body : Any> patchFromKtor(
        endpoint: String,
        requestBody: Body
    ): ExpectedResponse {
        return httpClient().patch {
            url("$scheme://$appHost/$apiPath/$endpoint")
            body = TextContent(Json.encodeToString(requestBody), ContentType.Application.Json)
        }
    }

    private fun httpClient() = HttpClient(httpClientEngine()) {
        val json = Json { ignoreUnknownKeys = true }
        install(JsonFeature) { serializer = KotlinxSerializer(json) }
        defaultRequest {
            header("accept", "application/json")
            header("Authorization", "Bearer $appJWTToken")
        }
    }
}

