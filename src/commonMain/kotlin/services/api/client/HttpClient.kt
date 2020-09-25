package services.api.client

internal expect class HttpClient {
    suspend inline fun <reified ExpectedResponse> get(
        endpoint: String,
        vararg urlParams: Pair<String, String>
    ): Response<ExpectedResponse>

    suspend inline fun <reified ExpectedResponse, reified Body : Any> patch(
        endpoint: String,
        requestBody: Body
    ): Response<ExpectedResponse>
}

data class Response<ExpectedType>(
    val headers: Map<String, String>,
    val body: ExpectedType
)