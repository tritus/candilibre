package api.client

internal actual class CandilibreClient {
    actual suspend inline fun <reified ExpectedResponse> get(
        endpoint: String,
        vararg urlParams: Pair<String, String>
    ): ExpectedResponse? {
        TODO("Not yet implemented")
    }

    actual suspend inline fun <reified ExpectedResponse, reified Body : Any> patch(
        endpoint: String,
        requestBody: Body
    ): ExpectedResponse? {
        TODO("Not yet implemented")
    }

}