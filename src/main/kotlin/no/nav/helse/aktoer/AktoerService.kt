package no.nav.helse.aktoer

import no.nav.helse.general.CallId
import no.nav.helse.general.auth.Fodselsnummer

class AktoerService(
    private val aktoerGateway: AktoerGateway
){
    suspend fun getAktorId(
        fnr: Fodselsnummer,
        callId: CallId
    ): AktoerId {
        return aktoerGateway.hentAktoerId(fnr, callId)
    }

    suspend fun getNorskIdent(
        aktoerId: AktoerId,
        callId: CallId
    ): NorskIdent {
        return aktoerGateway.hentNorskIdent(aktoerId, callId)
    }
}