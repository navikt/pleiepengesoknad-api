package no.nav.helse.mellomlagring

import com.github.fppt.jedismock.RedisServer
import no.nav.helse.redis.RedisConfig
import no.nav.helse.redis.RedisStore
import org.awaitility.Awaitility
import org.awaitility.Durations.ONE_SECOND
import org.junit.AfterClass
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MellomlagringTest {

    private companion object {
        val logger = LoggerFactory.getLogger(MellomlagringTest::class.java)

        val redisServer: RedisServer = RedisServer
            .newRedisServer()
            .started()

        val redisClient = RedisConfig.redisClient(
            redisHost = "localhost",
            redisPort = redisServer.bindPort
        )


        val redisStore = RedisStore(
            redisClient
        )

        val mellomlagringService = MellomlagringService(
            redisStore,
            "VerySecretPass",
            "1",
            "1"
        )

        @AfterClass
        @JvmStatic
        fun teardown() {
            redisClient.shutdown()
            redisServer.stop()
        }
    }

    @Test
    fun `mellomlagre verdier`() {
        mellomlagringService.setMellomlagring(MellomlagringPrefix.SØKNAD, "test", "søknad")
        val mellomlagring = mellomlagringService.getMellomlagring(MellomlagringPrefix.SØKNAD, "test")
        assertEquals("søknad", mellomlagring)

        mellomlagringService.setMellomlagring(MellomlagringPrefix.ENDRINGSMELDING, "test", "endringsmelding")
        val endringsmelding = mellomlagringService.getMellomlagring(MellomlagringPrefix.ENDRINGSMELDING, "test")
        assertEquals("endringsmelding", endringsmelding)
    }

    @Test
    fun `Oppdatering av mellomlagret verdi, skal ikke slette expiry`() {
        val key = "test"
        val expirationDate = Calendar.getInstance().let {
            it.add(Calendar.MINUTE, 1)
            it.time
        }

        val mellomlagringPrefix = MellomlagringPrefix.SØKNAD
        mellomlagringService.setMellomlagring(
            mellomlagringPrefix = mellomlagringPrefix,
            fnr = key,
            verdi = "test",
            expirationDate = expirationDate
        )
        val verdi = mellomlagringService.getMellomlagring(mellomlagringPrefix, key)
        assertEquals("test", verdi)
        val ttl = mellomlagringService.getTTLInMs(mellomlagringPrefix, key)
        assertNotEquals(ttl, -2)
        assertNotEquals(ttl, -1)

        logger.info("PTTL=$ttl")

        mellomlagringService.updateMellomlagring(MellomlagringPrefix.SØKNAD, key, "test2")
        val oppdatertVerdi = mellomlagringService.getMellomlagring(MellomlagringPrefix.SØKNAD, key)
        assertEquals("test2", oppdatertVerdi)
        assertNotEquals(ttl, -2)
        assertNotEquals(ttl, -1)
    }

    @Test
    fun `mellomlagret verdier skal være utgått etter 500 ms`() {
        val fnr = "12345678910"
        val søknad = "test"

        val expirationDate = Calendar.getInstance().let {
            it.add(Calendar.MILLISECOND, 500)
            it.time
        }
        mellomlagringService.setMellomlagring(MellomlagringPrefix.SØKNAD, fnr, søknad, expirationDate = expirationDate)
        val forventetVerdi1 = mellomlagringService.getMellomlagring(MellomlagringPrefix.SØKNAD, fnr)
        logger.info("Hentet mellomlagret verdi = {}", forventetVerdi1)
        assertEquals("test", forventetVerdi1)
        assertNotEquals(mellomlagringService.getTTLInMs(MellomlagringPrefix.SØKNAD, fnr), -2)
        assertNotEquals(mellomlagringService.getTTLInMs(MellomlagringPrefix.SØKNAD, fnr), -1)

        Awaitility.waitAtMost(ONE_SECOND).untilAsserted {
            val forventetVerdi2 = mellomlagringService.getMellomlagring(MellomlagringPrefix.SØKNAD, fnr)
            logger.info("Hentet mellomlagret verdi = {}", forventetVerdi2)
            assertNull(forventetVerdi2)
        }
    }

    @Test
    fun `verdier skal være krypterte`() {
        val fødselsnummer = "12345678910"
        mellomlagringService.setMellomlagring(MellomlagringPrefix.SØKNAD, fødselsnummer, "søknad")
        val mellomlagring = mellomlagringService.getMellomlagring(MellomlagringPrefix.SØKNAD, fødselsnummer)
        assertNotNull(redisStore.get("mellomlagring_$fødselsnummer"))
        assertNotEquals(mellomlagring, redisStore.get("mellomlagring_$fødselsnummer"))

        mellomlagringService.setMellomlagring(MellomlagringPrefix.ENDRINGSMELDING, fødselsnummer, "endringsmelding")
        val endringsmelding = mellomlagringService.getMellomlagring(MellomlagringPrefix.ENDRINGSMELDING, fødselsnummer)
        assertNotNull(redisStore.get("mellomlagring_endringsmelding_$fødselsnummer"))
        assertNotEquals(endringsmelding, redisStore.get("mellomlagring_endringsmelding_$fødselsnummer"))
    }

}
