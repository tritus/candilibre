package services.api.client

internal actual fun buildClient(
    scheme: String,
    appHost: String,
    apiPath: String,
    appJWTToken: String
) = HttpClient(scheme, appHost, apiPath, appJWTToken)