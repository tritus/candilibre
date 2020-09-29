package services.api.client

import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import services.api.client.errors.CandilibreClientBadRequestException
import services.api.client.errors.CandilibreClientBadResponseException
import services.api.client.errors.CandilibreClientBadTokenException
import services.user.UserService
import wininet.WinINetHelper
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume

internal actual class HttpClient(
    private val scheme: String,
    private val appHost: String,
    private val apiPath: String,
    private val appJWTToken: String
) {
    actual suspend inline fun <reified ExpectedResponse> get(
        endpoint: String,
        vararg urlParams: Pair<String, String>
    ): ExpectedResponse {
        coroutineContext.ensureActive()
        val params = urlParams.joinToString("&") { "${it.first}=${it.second}" }
        val path = listOf(appHost, apiPath, endpoint).filter { it.isNotEmpty() }.joinToString("/")
        val pathWithParams = listOf(path, params).filter { it.isNotEmpty() }.joinToString("?")
        val url = "$scheme://$pathWithParams"
        return getRaw(url).let(::decode)
    }

    private suspend fun getRaw(url: String): String {
        val headers = mapOf(
            "Authorization" to "Bearer $appJWTToken",
            "X-USER-ID" to UserService.getUserId(appJWTToken)
        )
        return wininetCall(url, "GET", headers, null)
    }

    actual suspend inline fun <reified ExpectedResponse, reified Body : Any> patch(
        endpoint: String,
        requestBody: Body
    ): ExpectedResponse {
        coroutineContext.ensureActive()
        val path = listOf(appHost, apiPath, endpoint).filter { it.isNotEmpty() }.joinToString("/")
        val url = "$scheme://$path"
        val body = Json { ignoreUnknownKeys = true }.encodeToString(requestBody)
        return patchRaw(url, body).let(::decode)
    }

    private suspend fun patchRaw(url: String, postData: String): String {
        val headers = mapOf(
            "Content-type" to "application/json",
            "Authorization" to "Bearer $appJWTToken",
            "X-USER-ID" to UserService.getUserId(appJWTToken)
        )
        return wininetCall(url, "PATCH", headers, postData)
    }

    private suspend fun wininetCall(
        url: String,
        method: String,
        headers: Map<String, String>,
        postData: String?
    ): String = suspendCancellableCoroutine { continuation ->
        println("Request $method on $url")
        try {
            WinINetHelper.request(url, method, headers, postData?.encodeToByteArray()) { code, body ->
                when (code) {
                    200 -> continuation.resume(body.decodeToString())
                    401 -> continuation.cancel(CandilibreClientBadTokenException(appJWTToken))
                    else -> continuation.cancel(CandilibreClientBadResponseException(code, url, body.decodeToString()))
                }
            }
        } catch (e: Throwable) {
            continuation.cancel(CandilibreClientBadRequestException(url, e))
        }
    }

    private inline fun <reified ExpectedResponse> decode(it: String): ExpectedResponse =
        Json { ignoreUnknownKeys = true }.decodeFromString(it)

}