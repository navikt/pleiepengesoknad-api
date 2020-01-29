package no.nav.helse.redis

import com.github.fppt.jedismock.RedisServer
import org.slf4j.LoggerFactory

object RedisMockUtil {
    private val log = LoggerFactory.getLogger(RedisMockUtil::class.java)
    private var mockedRedisServer = RedisServer.newRedisServer(6379)

    @JvmStatic
    fun startRedisMocked() {
        log.warn("Starter MOCKET in-memory redis-server. Denne meldingen skal du aldri se i prod")
        mockedRedisServer.start()
    }

    @JvmStatic
    fun stopRedisMocked() {
        mockedRedisServer.stop()
    }

}