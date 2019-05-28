package no.nav.helse.soker

import no.nav.helse.aktoer.NorskIdent
import no.nav.helse.general.CallId
import no.nav.helse.person.PersonService

class SokerService(
    private val personService: PersonService
) {
    suspend fun getSoker(
        norskIdent: NorskIdent,
        callId: CallId) : Soker {
        val person = personService.hentPerson(
            norskIdent = norskIdent,
            callId = callId
        )
        return Soker(
            fodselsnummer = norskIdent.getValue(), // TODO: Bør skifte til "alternativ_id" ?
            fodselsdato = person.fodselsdato,
            fornavn = person.fornavn,
            mellomnavn = person.mellomnavn,
            etternavn = person.etternavn
        )
    }
}