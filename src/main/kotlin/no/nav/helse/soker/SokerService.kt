package no.nav.helse.soker

import no.nav.helse.aktoer.AktoerService
import no.nav.helse.aktoer.NorskIdent
import no.nav.helse.general.CallId
import no.nav.helse.person.PersonService

class SokerService(
    private val personService: PersonService,
    private val aktoerService: AktoerService
) {
    suspend fun getSoker(
        norskIdent: NorskIdent,
        callId: CallId) : Soker {
        val aktoerId = aktoerService.getAktorId(norskIdent, callId)

        val person = personService.hentPerson(
            aktoerId = aktoerId,
            callId = callId
        )

        return Soker(
            aktoerId = aktoerId.value,
            fodselsnummer = norskIdent.getValue(), // TODO: Bør skifte til "alternativ_id" ?
            fodselsdato = person.fodselsdato,
            fornavn = person.fornavn,
            mellomnavn = person.mellomnavn,
            etternavn = person.etternavn
        )
    }
}