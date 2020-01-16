package no.nav.helse.mellomlagring

import no.nav.helse.general.CallId
import no.nav.helse.general.auth.IdToken
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MellomlagringService (
) {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(MellomlagringService::class.java)
        private val store = HashMap<String, String>()
    }

    suspend fun getMellomlagring(
        idToken: IdToken,
        callId: CallId
    ) : String? {
        return  store[idToken.value]
    }

    suspend fun setMellomlagring(
        idToken: IdToken,
        callId: CallId,
        midlertidigSøknad: String
    ) {
        store[idToken.value] = midlertidigSøknad
    }

    suspend fun deleteMellomlagring(
        idToken: IdToken,
        callId: CallId
    ) {
        store.remove(idToken.value)
    }
}