package api.client

import api.engine.httpClientEngine
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

internal actual class CandilibreClient(
    private val scheme: String,
    private val appHost: String,
    private val apiPath: String,
    private val appJWTToken: String
) {
    actual suspend inline fun <reified ExpectedResponse> get(
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

    actual suspend inline fun <reified ExpectedResponse, reified Body : Any> patch(
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
        url("${scheme}://${appHost}/${apiPath}/$endpoint?$params")
    }

    private suspend inline fun <reified ExpectedResponse, reified Body : Any> patchFromKtor(
        endpoint: String,
        requestBody: Body
    ): ExpectedResponse {
        return httpClient().patch {
            url("${scheme}://${appHost}/${apiPath}/$endpoint")
            body = TextContent(Json.encodeToString(requestBody), ContentType.Application.Json)
        }
    }

    private fun httpClient() = HttpClient(httpClientEngine()) {
        val json = Json { ignoreUnknownKeys = true }
        install(JsonFeature) { serializer = KotlinxSerializer(json) }
        defaultRequest {
            header("accept", "application/json")
            header("Authorization", "Bearer ${appJWTToken}")
        }
    }
}