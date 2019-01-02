package no.nav.helse.soker

import no.nav.helse.general.auth.Fodselsnummer
import no.nav.helse.general.extractFodselsdato
import java.time.LocalDate

class SokerService {
    fun getSoker(fnr: Fodselsnummer) : Soker {
        return Soker(
            fodselsnummer = fnr.value,
            fodselsdato = fodselsDato(fnr)
        )
    }

    private fun fodselsDato(fnr: Fodselsnummer) : LocalDate? {
        try {
            return extractFodselsdato(fnr)
        } catch (cause: Throwable) {
            return null
        }
    }
}