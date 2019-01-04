package no.nav.helse.barn

import java.time.LocalDate

data class Barn (
    val fodselsdato : LocalDate,
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String
)