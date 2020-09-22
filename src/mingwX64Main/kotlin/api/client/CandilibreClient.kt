package api.client

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import wininet.WinINetHelper
import kotlin.coroutines.resume

internal actual class CandilibreClient(
    private val scheme: String,
    private val appHost: String,
    private val apiPath: String,
    private val appJWTToken: String
) {
    actual suspend inline fun <reified ExpectedResponse> get(
        endpoint: String,
        vararg urlParams: Pair<String, String>
    ): ExpectedResponse? = try {
        val params = urlParams.joinToString("&") { "${it.first}=${it.second}" }
        val path = listOf(appHost, apiPath, endpoint).filter { it.isNotEmpty() }.joinToString("/")
        val pathWithParams = listOf(path, params).filter { it.isNotEmpty() }.joinToString("?")
        val url = "$scheme://$pathWithParams"
        getRaw(url).let(::decode)
    } catch (e: Throwable) {
        null
    }

    private suspend fun getRaw(url: String): String = suspendCancellableCoroutine { continuation ->
        WinINetHelper.request(url, "GET", mapOf(), null) { code, body ->
            if (code != 200) {
                continuation.cancel(IllegalStateException("BAD RESPONSE $code : ${body.decodeToString()}"))
            } else {
                continuation.resume(body.decodeToString())
            }
        }
    }

    actual suspend inline fun <reified ExpectedResponse, reified Body : Any> patch(
        endpoint: String,
        requestBody: Body
    ): ExpectedResponse? {
        TODO("Not yet implemented")
    }

    private inline fun <reified ExpectedResponse> decode(it: String): ExpectedResponse? =
        Json { ignoreUnknownKeys = true }.decodeFromString(it)

}