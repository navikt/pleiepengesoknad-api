package no.nav.helse.k9

import java.time.LocalDate

data class Person(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val foedselsdato: LocalDate
)