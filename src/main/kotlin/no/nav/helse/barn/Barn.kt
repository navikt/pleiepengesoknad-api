package no.nav.helse.barn

import java.time.LocalDate

data class BarnResponse(
    val barn: List<Barn>
)

data class Barn (
    val fødselsdato: LocalDate,
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String,
    val aktørId: String,
    val harSammeAdresse: Boolean
)
