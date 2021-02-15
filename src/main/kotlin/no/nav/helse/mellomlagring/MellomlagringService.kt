package no.nav.helse.mellomlagring

import io.ktor.util.*
import no.nav.helse.redis.RedisStore
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class MellomlagringService @KtorExperimentalAPI constructor(
    private val redisStore: RedisStore,
    private val passphrase: String
) {
    private companion object {
        private val log: Logger = LoggerFactory.getLogger(MellomlagringService::class.java)
    }

    private val nøkkelPrefiks = "mellomlagring_"

    fun getMellomlagring(
        fnr: String
    ): String? {
        val krypto = Krypto(passphrase, fnr)
        val encrypted = redisStore.get(nøkkelPrefiks + fnr) ?: return null
        return krypto.decrypt(encrypted)
    }

    fun setMellomlagring(
        fnr: String,
        midlertidigSøknad: String,
        expirationDate: Date = Calendar.getInstance().let {
            it.add(Calendar.MINUTE, 10)
            it.time
        }
    ) {
        val krypto = Krypto(passphrase, fnr)
        redisStore.set(nøkkelPrefiks + fnr, krypto.encrypt(midlertidigSøknad), expirationDate)
    }

    fun updateMellomlagring(
        fnr: String,
        midlertidigSøknad: String
    ) {
        val krypto = Krypto(passphrase, fnr)
        redisStore.update(nøkkelPrefiks + fnr, krypto.encrypt(midlertidigSøknad))
    }

    fun deleteMellomlagring(
        fnr: String
    ) {
        redisStore.delete(nøkkelPrefiks + fnr)
    }
}
