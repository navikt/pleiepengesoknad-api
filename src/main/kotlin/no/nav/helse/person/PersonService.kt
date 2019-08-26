package no.nav.helse.person

import no.nav.helse.aktoer.AktoerId
import no.nav.helse.general.CallId

class PersonService(
    private val personGateway: PersonGateway
) {
    suspend fun hentPerson(
        aktoerId: AktoerId,
        callId : CallId
    ) = personGateway.hentPerson(aktoerId, callId)
}