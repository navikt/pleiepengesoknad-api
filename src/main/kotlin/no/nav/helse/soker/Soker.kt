package no.nav.helse.soker

import java.time.LocalDate

data class Soker (
    val fodselsnummer: String,
    val fodselsdato : LocalDate?,
    val fornavn: String? = null,
    val mellomnavn: String? = null,
    val etternavn: String? = null
)