package no.nav.helse.soker

import java.time.LocalDate

data class Soker (
    val fodselsdato : LocalDate,
    val fornavn: String,
    val mellomnavn: String,
    val etternavn: String
)