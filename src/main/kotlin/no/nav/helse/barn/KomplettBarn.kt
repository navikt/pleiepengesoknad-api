package no.nav.helse.barn

import no.nav.helse.general.auth.Fodselsnummer
import java.time.LocalDate

data class KomplettBarn(
    val fodselsnummer: Fodselsnummer,
    val fodselsdato : LocalDate,
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String
)