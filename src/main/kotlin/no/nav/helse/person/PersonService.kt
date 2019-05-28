package no.nav.helse.person

import no.nav.helse.aktoer.AktoerId
import no.nav.helse.aktoer.AktoerService
import no.nav.helse.aktoer.NorskIdent
import no.nav.helse.general.CallId

class PersonService(
    private val aktoerService: AktoerService,
    private val personGateway: PersonGateway
) {
    suspend fun hentPerson(
        aktoerId: AktoerId,
        callId : CallId
    ) = personGateway.hentPerson(aktoerId, callId)

    suspend fun hentPerson(
        norskIdent: NorskIdent,
        callId: CallId
    ) = personGateway.hentPerson(aktoerService.getAktorId(norskIdent, callId), callId)
}