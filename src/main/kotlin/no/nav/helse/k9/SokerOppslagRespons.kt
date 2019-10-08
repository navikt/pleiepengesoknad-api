package no.nav.helse.k9

import java.time.LocalDate

data class SokerOppslagRespons(
    val aktør_id: String,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val fødselsdato: LocalDate
)