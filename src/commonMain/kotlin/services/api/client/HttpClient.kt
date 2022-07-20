package services.api.client

// TODO(arichard, 20/07/2022): add security headers inside http client
internal expect class HttpClient {
    suspend inline fun <reified ExpectedResponse> get(
        endpoint: String,
        vararg urlParams: Pair<String, String>
    ): ExpectedResponse

    suspend inline fun <reified ExpectedResponse, reified Body : Any> patch(
        endpoint: String,
        requestBody: Body
    ): ExpectedResponse
}