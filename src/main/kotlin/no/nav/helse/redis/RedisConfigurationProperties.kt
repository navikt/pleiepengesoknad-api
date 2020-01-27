package no.nav.helse.redis

import no.nav.helse.redis.RedisMockUtil.startRedisMocked

class RedisConfigurationProperties(private val redisMocked: Boolean) {

    fun startInMemoryRedisIfMocked() {
        if (redisMocked) {
            startRedisMocked()
        }
    }
}