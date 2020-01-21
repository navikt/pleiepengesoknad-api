package no.nav.helse.mellomlagring

import com.typesafe.config.ConfigFactory
import io.ktor.config.HoconApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import no.nav.helse.ApplicationTest
import no.nav.helse.Configuration
import no.nav.helse.TestConfiguration
import no.nav.helse.redis.RedisConfig
import no.nav.helse.redis.RedisConfigurationProperties
import no.nav.helse.redis.RedisMockUtil
import no.nav.sbl.sosialhjelp.login.api.redis.RedisStore
import org.junit.AfterClass
import kotlin.test.*

@KtorExperimentalAPI
class MellomlagringTest {
    private companion object {
        val redisClient = RedisConfig(RedisConfigurationProperties(true)).redisClient(
            Configuration(
                HoconApplicationConfig(ConfigFactory.parseMap(TestConfiguration.asMap()))
            )
        )
        val redisStore = RedisStore(
            redisClient
        )
        val mellomlagringService = MellomlagringService(
            redisStore,
            "VerySecretPass"
        )

        @AfterClass
        @JvmStatic
        fun teardown() {
            redisClient.shutdown()
            RedisMockUtil.stopRedisMocked()
        }
    }

    @Test
    internal fun `mellomlagre verdier`() {
        mellomlagringService.setMellomlagring("test", "test")

        val mellomlagring = mellomlagringService.getMellomlagring("test")

        assertEquals("test", mellomlagring)
    }

    @Test
    internal fun `verdier skal være krypterte`() {

        mellomlagringService.setMellomlagring("test", "test")

        val mellomlagring = mellomlagringService.getMellomlagring("test")
        assertNotNull(redisStore.getString("test"))
        assertNotEquals(mellomlagring, redisStore.getString("test"))
    }

}