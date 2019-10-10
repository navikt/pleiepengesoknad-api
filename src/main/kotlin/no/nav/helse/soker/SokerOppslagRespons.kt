package no.nav.helse.soker

import java.time.LocalDate

data class SokerOppslagRespons(
    val aktør_id: String,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val fødselsdato: LocalDate
)