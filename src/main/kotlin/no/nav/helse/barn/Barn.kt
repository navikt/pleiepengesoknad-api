package no.nav.helse.barn

import no.nav.helse.aktoer.AktoerId
import java.time.LocalDate

data class Barn (
    val fodselsdato: LocalDate,
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String,
    val aktoerId: AktoerId,
    val status : String
)

fun Barn.tilDto() = BarnDTO(
    fodselsdato = fodselsdato,
    fornavn = fornavn,
    mellomnavn = mellomnavn,
    etternavn = etternavn,
    aktoerId = aktoerId.value
)

data class BarnDTO (
    val fodselsdato: LocalDate,
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String,
    val aktoerId: String
)