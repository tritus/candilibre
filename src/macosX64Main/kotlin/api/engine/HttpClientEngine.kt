package api.engine

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.curl.Curl
import io.ktor.util.KtorExperimentalAPI

@OptIn(KtorExperimentalAPI::class)
internal actual fun httpClientEngine(): HttpClientEngineFactory<*> = Curl