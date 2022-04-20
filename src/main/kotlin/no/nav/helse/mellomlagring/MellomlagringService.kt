package no.nav.helse.mellomlagring

import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.helse.general.CallId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime

enum class MellomlagringPrefix {
    SØKNAD, ENDRINGSMELDING;

    fun nøkkelPrefiks() = when (this) {
        SØKNAD -> "mellomlagring_psb"
        ENDRINGSMELDING -> "mellomlagring_endringsmelding_psb"
    }
}

class MellomlagringService(
    private val søknadMellomlagretTidTimer: String,
    private val endringsmeldingMellomlagretTidTimer: String,
    private val k9BrukerdialogCacheGateway: K9BrukerdialogCacheGateway
) {
    private companion object {
        private val log: Logger = LoggerFactory.getLogger(MellomlagringService::class.java)
    }

    suspend fun getMellomlagring(
        mellomlagringPrefix: MellomlagringPrefix,
        idToken: IdToken,
        callId: CallId
    ): String? {
        return k9BrukerdialogCacheGateway.hentMellomlagretSøknad(
            nøkkelPrefiks = mellomlagringPrefix.nøkkelPrefiks(),
            idToken = idToken,
            callId = callId
        )?.verdi
    }

    suspend fun setMellomlagring(
        mellomlagringPrefix: MellomlagringPrefix,
        verdi: String,
        utløpsdato: ZonedDateTime = when (mellomlagringPrefix) {
            MellomlagringPrefix.SØKNAD -> ZonedDateTime.now(UTC).plusHours(søknadMellomlagretTidTimer.toLong())
            MellomlagringPrefix.ENDRINGSMELDING -> ZonedDateTime.now(UTC).plusHours(endringsmeldingMellomlagretTidTimer.toLong())
        },
        idToken: IdToken,
        callId: CallId
    ): CacheResponseDTO? {
        return k9BrukerdialogCacheGateway.mellomlagreSøknad(
            cacheRequestDTO = CacheRequestDTO(
                nøkkelPrefiks = mellomlagringPrefix.nøkkelPrefiks(),
                verdi = verdi,
                utløpsdato = utløpsdato,
                opprettet = ZonedDateTime.now(UTC),
                endret = null
            ),
            idToken = idToken,
            callId = callId
        )
    }

    suspend fun updateMellomlagring(
        mellomlagringPrefix: MellomlagringPrefix,
        verdi: String,
        idToken: IdToken,
        callId: CallId
    ): CacheResponseDTO {
        val eksisterendeMellomlagring = k9BrukerdialogCacheGateway.hentMellomlagretSøknad(
            nøkkelPrefiks = mellomlagringPrefix.nøkkelPrefiks(),
            idToken = idToken,
            callId = callId
        ) ?: throw CacheNotFoundException(mellomlagringPrefix.nøkkelPrefiks())

        return k9BrukerdialogCacheGateway.oppdaterMellomlagretSøknad(
            cacheRequestDTO = CacheRequestDTO(
                nøkkelPrefiks = mellomlagringPrefix.nøkkelPrefiks(),
                verdi = verdi,
                utløpsdato = eksisterendeMellomlagring.utløpsdato,
                opprettet = eksisterendeMellomlagring.opprettet,
                endret = ZonedDateTime.now(UTC)
            ),
            idToken = idToken,
            callId = callId
        )
    }

    suspend fun deleteMellomlagring(
        mellomlagringPrefix: MellomlagringPrefix,
        idToken: IdToken,
        callId: CallId
    ): Boolean = k9BrukerdialogCacheGateway.slettMellomlagretSøknad(
        nøkkelPrefiks = mellomlagringPrefix.nøkkelPrefiks(),
        idToken = idToken,
        callId = callId
    )
}
