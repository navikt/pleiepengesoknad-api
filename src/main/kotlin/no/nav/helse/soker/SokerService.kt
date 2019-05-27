package no.nav.helse.soker

import no.nav.helse.general.CallId
import no.nav.helse.general.auth.Fodselsnummer
import no.nav.helse.person.PersonService

class SokerService(
    private val personService: PersonService
) {
    suspend fun getSoker(
        fnr: Fodselsnummer,
        callId: CallId) : Soker {
        val person = personService.hentPerson(
            fnr = fnr,
            callId = callId
        )
        return Soker(
            fodselsnummer = fnr.value,
            fodselsdato = person.fodselsdato,
            fornavn = person.fornavn,
            mellomnavn = person.mellomnavn,
            etternavn = person.etternavn
        )
    }
}