package no.nav.helse.mellomlagring

import io.ktor.util.KtorExperimentalAPI
import no.nav.sbl.sosialhjelp.login.api.redis.RedisStore
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class MellomlagringService @KtorExperimentalAPI constructor(private val redisStore: RedisStore,private val passphrase:String) {
    private companion object {
        private val log: Logger = LoggerFactory.getLogger(MellomlagringService::class.java)
    }

    fun getMellomlagring(
        fnr: String
    ): String? {
        log.info("Get " + fnr)
        val krypto = Krypto(passphrase, fnr)
        val encrypted = redisStore.get(fnr) ?: return null
        return krypto.decrypt(encrypted)
    }

    fun setMellomlagring(
        fnr: String,
        midlertidigSøknad: String
    ) {
        log.info("Set " + fnr)
        val krypto = Krypto(passphrase, fnr)
        val expirationDate = Calendar.getInstance().let {
            it.add(Calendar.HOUR, 24)
            it.time
        }
        redisStore.set(fnr, krypto.encrypt(midlertidigSøknad),expirationDate)
    }

    fun deleteMellomlagring(
        fnr: String
    ) {
        redisStore.delete(fnr)
    }
}