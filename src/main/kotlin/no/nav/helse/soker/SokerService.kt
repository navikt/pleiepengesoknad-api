package no.nav.helse.soker

import no.nav.helse.general.auth.Fodselsnummer
import java.time.LocalDate

class SokerService() {
    fun getSoker(fnr: Fodselsnummer) : Soker {
        return Soker(
            fornavn = "Erik",
            mellomnavn = "Maximilian",
            etternavn = "Forsman",
            fodselsdato = LocalDate.now()
        )
    }
}