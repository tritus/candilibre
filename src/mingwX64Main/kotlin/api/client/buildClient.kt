package api.client

internal actual fun buildClient(
    scheme: String,
    appHost: String,
    apiPath: String,
    appJWTToken: String
) = CandilibreClient(scheme, appHost, apiPath, appJWTToken)