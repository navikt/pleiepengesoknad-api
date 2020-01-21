package no.nav.helse.redis

import io.ktor.util.KtorExperimentalAPI
import io.lettuce.core.RedisClient
import no.nav.helse.Configuration
import org.slf4j.LoggerFactory

class RedisConfig(private val redisConfigurationProperties: RedisConfigurationProperties) {
    private val log = LoggerFactory.getLogger(RedisConfig::class.java)

    @KtorExperimentalAPI
    fun redisClient(configuration: Configuration): RedisClient {
        redisConfigurationProperties.startInMemoryRedisIfMocked()
        log.info("redis://${configuration.getRedisHost()}:${configuration.getRedisPort()}")
        return RedisClient.create("redis://${configuration.getRedisHost()}:${configuration.getRedisPort()}")
    }

}