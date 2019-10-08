package no.nav.helse.k9

import no.nav.helse.aktoer.AktoerId
import java.time.LocalDate

data class Barn (
    val fødselsdato: LocalDate,
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String,
    val aktør_id: String
)

fun Barn.tilDto() = BarnDTO(
    fodselsdato = fødselsdato,
    fornavn = fornavn,
    mellomnavn = mellomnavn,
    etternavn = etternavn,
    aktoerId = aktør_id
)

data class BarnDTO (
    val fodselsdato: LocalDate,
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String,
    val aktoerId: String
)