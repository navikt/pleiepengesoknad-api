package no.nav.helse.k9

import no.nav.helse.aktoer.AktoerId
import java.time.LocalDate

data class Barn (
    val fodselsdato: LocalDate,
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String,
    val aktoerId: AktoerId
)