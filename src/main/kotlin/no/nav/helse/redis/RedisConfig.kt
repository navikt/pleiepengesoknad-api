package no.nav.helse.redis

import io.ktor.util.KtorExperimentalAPI
import io.lettuce.core.RedisClient
import no.nav.helse.Configuration
import org.slf4j.LoggerFactory

class RedisConfig(private val redisConfigurationProperties: RedisConfigurationProperties) {

    @KtorExperimentalAPI
    fun redisClient(configuration: Configuration): RedisClient {
        redisConfigurationProperties.startInMemoryRedisIfMocked()
        return RedisClient.create("redis://${configuration.getRedisHost()}:${configuration.getRedisPort()}")
    }

}