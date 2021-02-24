package services.api.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.curl.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.posix.FILE
import platform.posix.NULL
import platform.posix.exit
import platform.posix.fgets
import platform.posix.pclose
import platform.posix.popen
import platform.posix.printf
import services.api.client.errors.CandilibreClientBadTokenException
import services.user.UserService

internal actual class HttpClient(
    private val scheme: String,
    private val appHost: String,
    private val apiPath: String,
    private val appJWTToken: String
) {
    private val httpClient: HttpClient = io.ktor.client.HttpClient(Curl)

    actual suspend inline fun <reified ExpectedResponse> get(
        endpoint: String,
        vararg urlParams: Pair<String, String>
    ): ExpectedResponse {
        return try {
            getFromKtor<ExpectedResponse>(endpoint, *urlParams).also { println("API CALL SUCCESS on $endpoint : $it") }
        } catch (e: Throwable) {
            println("API CALL ERROR on $endpoint : ${e.message}")
            throw e
        }
    }

    actual suspend inline fun <reified ExpectedResponse, reified Body : Any> patch(
        endpoint: String,
        requestBody: Body
    ): ExpectedResponse {
        return try {
            patchFromKtor<ExpectedResponse, Body>(endpoint, requestBody)
                .also { println("API CALL SUCCESS on $endpoint : $it") }
        } catch (e: Throwable) {
            println("API CALL ERROR on $endpoint : ${e.message}")
            throw e
        }
    }

    private suspend inline fun <reified ExpectedResponse> getFromKtor(
        endpoint: String,
        vararg urlParams: Pair<String, String>
    ): ExpectedResponse {
        val params = urlParams.joinToString("&") { "${it.first}=${(it.second).encodeURLParameter()}" }
        val url = "$scheme://$appHost/$apiPath/$endpoint?$params"
        val response = httpClient.get<HttpResponse>(url) {
            headers {
                append("Accept", "application/json")
                append("Authorization", "Bearer $appJWTToken")
                append("X-USER-ID", UserService.getUserId(appJWTToken))
            }
        }
        val responseBody = response.readText()
        return try {
            decodeResponse(responseBody)
        } catch (e: Throwable) {
            println("ERROR while decoding json $responseBody")
            throw e
        }
    }

    private suspend inline fun <reified ExpectedResponse, reified Body : Any> patchFromKtor(
        endpoint: String,
        requestBody: Body
    ): ExpectedResponse {
        val url = "$scheme://$appHost/$apiPath/$endpoint"
        val response = httpClient.patch<HttpResponse>(url) {
            headers {
                append("Accept", "application/json")
                append("Authorization", "Bearer $appJWTToken")
                append("X-USER-ID", UserService.getUserId(appJWTToken))
            }
            body = Json.encodeToString(requestBody).replace("\"", "\\\"")
        }
        val responseBody = response.readText()
        return try {
            decodeResponse(responseBody)
        } catch (e: Throwable) {
            println("ERROR while decoding json $responseBody")
            throw e
        }
    }

    private inline fun <reified ExpectedResponse> decodeResponse(response: String): ExpectedResponse {
        Json { ignoreUnknownKeys = true }
            .runCatching { decodeFromString<BadTokenResponse>(response) }
            .getOrNull()
            ?.let { if (!it.isTokenValid) throw CandilibreClientBadTokenException(appJWTToken) }
        return Json { ignoreUnknownKeys = true }.decodeFromString(response)
    }

    @Serializable
    private data class BadTokenResponse(val isTokenValid: Boolean)
}