package api.client

internal expect fun buildClient(scheme: String, appHost: String, apiPath: String, appJWTToken: String): CandilibreClient