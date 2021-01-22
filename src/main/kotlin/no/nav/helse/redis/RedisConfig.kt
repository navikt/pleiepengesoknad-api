package no.nav.helse.redis

import io.ktor.util.KtorExperimentalAPI
import io.lettuce.core.RedisClient
import no.nav.helse.Configuration

internal object RedisConfig {

    @KtorExperimentalAPI
    internal fun redisClient(redisHost: String, redisPort: Int): RedisClient {
        return RedisClient.create("redis://${redisHost}:${redisPort}")
    }

}