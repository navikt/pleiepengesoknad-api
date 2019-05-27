package no.nav.helse.person

import java.time.LocalDate

data class Person(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val fodselsdato: LocalDate
)