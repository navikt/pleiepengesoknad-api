package no.nav.helse.soker

import no.nav.helse.general.CallId
import no.nav.helse.general.auth.Fodselsnummer

class SokerService(
    private val sokerGateway: SokerGateway
) {
    suspend fun getSoker(
        fnr: Fodselsnummer,
        callId: CallId) : Soker {
        return sokerGateway.getSoker(
            fnr = fnr,
            callId = callId
        )
    }
}