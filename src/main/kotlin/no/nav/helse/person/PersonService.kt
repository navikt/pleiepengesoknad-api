package no.nav.helse.person

import no.nav.helse.aktoer.AktoerId
import no.nav.helse.aktoer.AktoerService
import no.nav.helse.general.CallId
import no.nav.helse.general.auth.Fodselsnummer

class PersonService(
    private val aktoerService: AktoerService,
    private val personGateway: PersonGateway
) {
    suspend fun hentPerson(
        aktoerId: AktoerId,
        callId : CallId
    ) = personGateway.hentPerson(aktoerId, callId)

    suspend fun hentPerson(
        fnr: Fodselsnummer,
        callId: CallId
    ) = personGateway.hentPerson(aktoerService.getAktorId(fnr, callId), callId)
}