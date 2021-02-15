package no.nav.helse.mellomlagring

import com.github.fppt.jedismock.RedisServer
import io.ktor.util.*
import no.nav.helse.redis.RedisConfig
import no.nav.helse.redis.RedisStore
import org.awaitility.Awaitility
import org.awaitility.Durations.ONE_SECOND
import org.junit.AfterClass
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.test.*

@KtorExperimentalAPI
class MellomlagringTest {

    private companion object {
        val logger = LoggerFactory.getLogger(MellomlagringTest::class.java)

        val redisServer: RedisServer = RedisServer
            .newRedisServer(6379)
            .started()

        val redisClient = RedisConfig.redisClient(
            redisHost = redisServer.host,
            redisPort = redisServer.bindPort
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
            redisServer.stop()
        }
    }

    @Test
    internal fun `mellomlagre verdier`() {
        mellomlagringService.setMellomlagring("test", "test")

        val mellomlagring = mellomlagringService.getMellomlagring("test")

        assertEquals("test", mellomlagring)
    }

    @Test
    internal fun `mellomlagret verdier skal være utgått etter 500 ms`() {
        val fnr = "12345678910"
        val søknad = "test"

        mellomlagringService.setMellomlagring(fnr, søknad, expirationDate = Calendar.getInstance().let {
            it.add(Calendar.MILLISECOND, 500)
            it.time
        })
        val forventetVerdi1 = mellomlagringService.getMellomlagring(fnr)
        logger.info("Hentet mellomlagret verdi = {}", forventetVerdi1)
        assertEquals("test", forventetVerdi1)

        Awaitility.waitAtMost(ONE_SECOND).untilAsserted {
            val forventetVerdi2 = mellomlagringService.getMellomlagring(fnr)
            logger.info("Hentet mellomlagret verdi = {}", forventetVerdi2)
            assertNull(forventetVerdi2)
        }
    }

    @Test
    internal fun `verdier skal være krypterte`() {

        mellomlagringService.setMellomlagring("test", "test")

        val mellomlagring = mellomlagringService.getMellomlagring("test")
        assertNotNull(redisStore.get("mellomlagring_test"))
        assertNotEquals(mellomlagring, redisStore.get("test"))
    }

}
