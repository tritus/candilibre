package api

import api.model.Centre
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

internal object CandilibApi {
    private val scheme = "https"
    private val appHost = "beta.interieur.gouv.fr/candilib"
    private val apiPath = "api/v2"
    private val appJWTToken =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjVmMmQwYmNmZThiN2FiMDAxM2EyZjhmNyIsImxldmVsIjowLCJpYXQiOjE1OTk2ODU3NDYsImV4cCI6MTU5OTk0NDk0Nn0.A1somEp7z5wEQwnBJJt8dnOLyxJvEYttxe4aGm0DlDA"

    suspend fun getCentres(depNumber: String): List<Centre>? =
        get("candidat/centres", "departement" to depNumber)

    private suspend inline fun <reified ExpectedResponse> get(
        endpoint: String,
        vararg urlParams: Pair<String, String>
    ): ExpectedResponse? {
        return try {
            getFromKtor<ExpectedResponse>(endpoint, *urlParams).also { println("API CALL SUCCESS on $endpoint : $it") }
        } catch (e: ClientRequestException) {
            println("API CALL ERROR : ${e.message}")
            null
        } catch (e: Throwable) {
            throw e
        }
    }

    private suspend inline fun <reified ExpectedResponse> getFromKtor(
        endpoint: String,
        vararg urlParams: Pair<String, String>
    ): ExpectedResponse {
        return HttpClient().get<String> {
            val params = urlParams.joinToString("&") { "${it.first}=${it.second}" }
            val urlString = "$scheme://$appHost/$apiPath/$endpoint?$params"
            println(urlString)
            url(urlString)
            header("accept", "application/json")
            header("Authorization", "Bearer $appJWTToken")
        }.let {
            Json { ignoreUnknownKeys = true }.decodeFromString(it)
        }
    }
}