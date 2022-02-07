package no.nav.helse.mellomlagring

import no.nav.helse.redis.RedisStore
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

enum class MellomlagringPrefix {
    SØKNAD, ENDRINGSMELDING;

    fun nøkkelPrefiks() = when (this) {
        SØKNAD -> "mellomlagring_"
        ENDRINGSMELDING -> "mellomlagring_endringsmelding_"
    }
}

class MellomlagringService(
    private val redisStore: RedisStore,
    private val passphrase: String,
    private val søknadMellomlagretTidTimer: String,
    private val endringsmeldingMellomlagretTidTimer: String
) {
    private companion object {
        private val log: Logger = LoggerFactory.getLogger(MellomlagringService::class.java)
    }

    fun getMellomlagring(
        mellomlagringPrefix: MellomlagringPrefix,
        fnr: String
    ): String? {
        val krypto = Krypto(passphrase, fnr)
        val encrypted = redisStore.get(mellomlagringPrefix.nøkkelPrefiks() + fnr) ?: return null
        return krypto.decrypt(encrypted)
    }

    fun setMellomlagring(
        mellomlagringPrefix: MellomlagringPrefix,
        fnr: String,
        verdi: String,
        expirationDate: Date = Calendar.getInstance().let {
            it.add(
                Calendar.HOUR, when (mellomlagringPrefix) {
                    MellomlagringPrefix.SØKNAD -> søknadMellomlagretTidTimer.toInt()
                    MellomlagringPrefix.ENDRINGSMELDING -> endringsmeldingMellomlagretTidTimer.toInt()
                }
            )
            it.time
        }
    ) {
        val krypto = Krypto(passphrase, fnr)
        redisStore.set(mellomlagringPrefix.nøkkelPrefiks() + fnr, krypto.encrypt(verdi), expirationDate)
    }

    fun updateMellomlagring(
        mellomlagringPrefix: MellomlagringPrefix,
        fnr: String,
        verdi: String
    ) {
        val krypto = Krypto(passphrase, fnr)
        redisStore.update(mellomlagringPrefix.nøkkelPrefiks() + fnr, krypto.encrypt(verdi))
    }

    fun deleteMellomlagring(mellomlagringPrefix: MellomlagringPrefix, fnr: String) =
        redisStore.delete(mellomlagringPrefix.nøkkelPrefiks() + fnr)


    fun getTTLInMs(mellomlagringPrefix: MellomlagringPrefix, fnr: String): Long =
        redisStore.getPTTL(mellomlagringPrefix.nøkkelPrefiks() + fnr)
}
