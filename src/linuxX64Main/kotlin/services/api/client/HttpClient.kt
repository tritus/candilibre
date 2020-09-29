package services.api.client

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.refTo
import kotlinx.coroutines.coroutineScope
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
    ): ExpectedResponse {
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
    ): ExpectedResponse {
        val params = urlParams.joinToString("&") { "${it.first}=${it.second}" }
        val url = "$scheme://$appHost/$apiPath/$endpoint?$params"
        val command = "curl -X GET \"$url\" -H \"accept: application/json\" -H \"Authorization: Bearer $appJWTToken\""
        val result = executeCommand(command)
        return Json { ignoreUnknownKeys = true }.decodeFromString(result)
    }

    private suspend inline fun <reified ExpectedResponse, reified Body : Any> patchFromKtor(
        endpoint: String,
        requestBody: Body
    ): ExpectedResponse {
        val url = "$scheme://$appHost/$apiPath/$endpoint"
        val body = Json.encodeToString(requestBody).replace("\"", "\\\"")
        val command =
            "curl -X PATCH \"$url\" -H \"accept: application/json\" -H \"Authorization: Bearer $appJWTToken\" -H \"Content-Type: application/json\" -d \"$body\""
        val result = executeCommand(command)
        return try {
            Json { ignoreUnknownKeys = true }.decodeFromString(result)
        } catch (e: Throwable) {
            println("ERROR while decoding json $result")
            throw e
        }

    }

    private suspend fun executeCommand(command: String): String = coroutineScope {
        val fp: CPointer<FILE>? = popen(command, "r")
        val buffer = ByteArray(4096)
        val returnString = StringBuilder()

        /* Open the command for reading. */
        if (fp == NULL) {
            printf("Failed to run command\n")
            exit(1)
        }

        /* Read the output a line at a time - output it. */
        var scan = fgets(buffer.refTo(0), buffer.size, fp)
        if (scan != null) {
            while (scan != NULL) {
                returnString.append(scan!!.toKString())
                scan = fgets(buffer.refTo(0), buffer.size, fp)
            }
        }
        /* close */
        pclose(fp)
        returnString.trim().toString()
    }
}