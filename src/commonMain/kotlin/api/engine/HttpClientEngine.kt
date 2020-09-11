package api.engine

import io.ktor.client.engine.HttpClientEngineFactory

internal expect fun httpClientEngine(): HttpClientEngineFactory<*>