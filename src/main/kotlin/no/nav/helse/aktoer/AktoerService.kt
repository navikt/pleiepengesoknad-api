package no.nav.helse.aktoer

import no.nav.helse.general.CallId

class AktoerService(
    private val aktoerGateway: AktoerGateway
){
    suspend fun getNorskIdent(
        aktoerId: AktoerId,
        callId: CallId
    ): NorskIdent {
        return aktoerGateway.hentNorskIdent(aktoerId, callId)
    }
}